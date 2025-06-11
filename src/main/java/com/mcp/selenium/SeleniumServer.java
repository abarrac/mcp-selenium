package com.mcp.selenium;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.util.concurrent.CountDownLatch;

/**
 * MCP Selenium Server - Model Context Protocol implementation for browser automation.
 * <p>
 * This server implements the MCP (Model Context Protocol) specification, providing
 * a standardized JSON-RPC interface for AI agents to interact with Selenium WebDriver.
 * Handles protocol negotiation, tool discovery, and command execution.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Full MCP 2024-11-05 protocol compliance</li>
 *   <li>Bidirectional JSON-RPC communication</li>
 *   <li>Comprehensive tool discovery and validation</li>
 *   <li>Graceful error handling and recovery</li>
 *   <li>Multi-threaded request processing</li>
 * </ul>
 * 
 * <p>Protocol Flow:
 * <ol>
 *   <li>Initialize - Negotiate protocol version and capabilities</li>
 *   <li>Tool Discovery - List available automation tools</li>
 *   <li>Tool Execution - Process automation requests</li>
 *   <li>Cleanup - Handle graceful shutdown</li>
 * </ol>
 * 
 * @author Alberto Barragán
 * @version 1.0.0
 * @since 1.0.0
 */
public class SeleniumServer {
    /** JSON object mapper configured for MCP protocol requirements */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /** Selenium automation engine instance */
    private static SeleniumMCP seleniumMCP;
    
    /** Server running state - thread-safe flag */
    private static volatile boolean running = true;
    
    /** Synchronization primitive for graceful shutdown */
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    static {
        // Configure JSON mapper for streaming operation
        objectMapper.getFactory().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    }

    /**
     * Main entry point for the MCP Selenium Server.
     * <p>
     * Initializes the server, sets up shutdown hooks, and enters the main
     * message processing loop. Handles stdin/stdout communication as per
     * MCP specification.
     * 
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutdown hook triggered");
            running = false;
            shutdownLatch.countDown();
            if (seleniumMCP != null) {
                seleniumMCP.cleanup();
            }
        }));

        try {
            System.err.println("Starting MCP Selenium Server...");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out), true);

            // Main message loop
            Thread readerThread = new Thread(() -> {
                try {
                    while (running) {
                        String line = reader.readLine();

                        if (line == null) {
                            System.err.println("EOF on stdin");
                            Thread.sleep(100);
                            continue;
                        }

                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        System.err.println("Received: " + line);

                        try {
                            ObjectNode request = objectMapper.readValue(line, ObjectNode.class);
                            ObjectNode response = handleRequest(request);

                            if (response != null) {
                                String responseStr = objectMapper.writeValueAsString(response);
                                System.err.println("Sending: " + responseStr);
                                writer.println(responseStr);
                                writer.flush();
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing message: " + e.getMessage());
                            e.printStackTrace(System.err);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Reader thread error: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            });

            readerThread.start();

            // Wait for shutdown
            shutdownLatch.await();

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        System.err.println("Server exiting");
    }

    /**
     * Processes incoming MCP requests and generates appropriate responses.
     * <p>
     * Implements the core MCP protocol handling, including:
     * <ul>
     *   <li>Protocol initialization and capability negotiation</li>
     *   <li>Tool discovery and schema generation</li>
     *   <li>Tool execution with error handling</li>
     *   <li>Resource and prompt management (not implemented)</li>
     * </ul>
     * 
     * @param request the incoming JSON-RPC request object
     * @return response object, or null for notifications
     */
    private static ObjectNode handleRequest(ObjectNode request) {
        try {
            String method = request.get("method").asText();
            String id = request.has("id") ? request.get("id").asText() : null;

            System.err.println("Handling method: " + method);

            // Handle notifications (no response needed)
            if (method.equals("notifications/initialized")) {
                System.err.println("Received initialized notification");
                return null;
            }

            // Create response
            ObjectNode response = objectMapper.createObjectNode();
            response.put("jsonrpc", "2.0");
            if (id != null) {
                response.put("id", id);
            }

            switch (method) {
                case "initialize":
                    handleInitialize(response);
                    break;

                case "tools/list":
                    handleToolsList(response);
                    break;

                case "tools/call":
                    handleToolCall(response, request.get("params"));
                    break;

                case "resources/list":
                    // We don't support resources
                    ObjectNode resourcesResult = objectMapper.createObjectNode();
                    resourcesResult.set("resources", objectMapper.createArrayNode());
                    response.set("result", resourcesResult);
                    break;

                case "prompts/list":
                    // We don't support prompts
                    ObjectNode error = objectMapper.createObjectNode();
                    error.put("code", -32601);
                    error.put("message", "Method not found");
                    response.set("error", error);
                    break;

                default:
                    ObjectNode defaultError = objectMapper.createObjectNode();
                    defaultError.put("code", -32601);
                    defaultError.put("message", "Method not found: " + method);
                    response.set("error", defaultError);
            }

            return response;

        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            e.printStackTrace(System.err);

            if (request.has("id")) {
                ObjectNode response = objectMapper.createObjectNode();
                response.put("jsonrpc", "2.0");
                response.put("id", request.get("id").asText());

                ObjectNode error = objectMapper.createObjectNode();
                error.put("code", -32603);
                error.put("message", "Internal error: " + e.getMessage());
                response.set("error", error);

                return response;
            }

            return null;
        }
    }

    /**
     * Handles MCP protocol initialization requests.
     * 
     * Negotiates protocol version, declares server capabilities, and provides
     * server metadata. Creates the Selenium automation engine instance.
     * 
     * @param response the response object to populate
     */
    private static void handleInitialize(ObjectNode response) {
        if (seleniumMCP == null) {
            seleniumMCP = new SeleniumMCP();
        }

        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", "2024-11-05");

        ObjectNode capabilities = objectMapper.createObjectNode();
        ObjectNode tools = objectMapper.createObjectNode();
        tools.put("listChanged", true);
        capabilities.set("tools", tools);

        ObjectNode resources = objectMapper.createObjectNode();
        resources.put("listChanged", true);
        capabilities.set("resources", resources);

        result.set("capabilities", capabilities);

        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", "MCP Selenium");
        serverInfo.put("version", "1.0.0");
        result.set("serverInfo", serverInfo);

        response.set("result", result);
    }

    /**
     * Generates comprehensive tool discovery response.
     * 
     * Creates detailed JSON schema for all available automation tools,
     * including parameter validation, descriptions, and enumerated values.
     * Supports AI agents' dynamic tool discovery and validation.
     * 
     * @param response the response object to populate with tool definitions
     */
    private static void handleToolsList(ObjectNode response) {
        ArrayNode tools = objectMapper.createArrayNode();

        // Browser control
        addTool(tools, "start_browser", "launches browser",
                new String[]{"browser"},
                new Object[][]{
                        {"browser", "string", "Browser to launch (chrome or firefox)", new String[]{"chrome", "firefox"}},
                        {"options", "object", "Browser options", null}
                });

        // Navigation tools
        addTool(tools, "navigate", "navigates to a URL",
                new String[]{"url"},
                new Object[][]{
                        {"url", "string", "URL to navigate to", null}
                });

        addTool(tools, "goBack", "navigate back in browser history",
                new String[]{},
                new Object[][]{});

        addTool(tools, "goForward", "navigate forward in browser history",
                new String[]{},
                new Object[][]{});

        addTool(tools, "refresh", "refresh the current page",
                new String[]{},
                new Object[][]{});

        addTool(tools, "getCurrentUrl", "get the current URL",
                new String[]{},
                new Object[][]{});

        addTool(tools, "getTitle", "get the page title",
                new String[]{},
                new Object[][]{});

        // Element finding
        addTool(tools, "find_element", "finds an element",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null},
                        {"timeout", "number", "Maximum time to wait for element in milliseconds", null}
                });

        addTool(tools, "findElements", "find all elements matching selector",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find elements", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null}
                });

        addTool(tools, "waitForElement", "wait for element to appear",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null},
                        {"timeout", "number", "Timeout in seconds (default: 10)", null}
                });

        // Element interaction
        addTool(tools, "click_element", "clicks an element",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null},
                        {"timeout", "number", "Maximum time to wait for element in milliseconds", null}
                });

        addTool(tools, "doubleClick", "double-click on an element",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null}
                });

        addTool(tools, "rightClick", "right-click on an element",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null}
                });

        addTool(tools, "hover", "hover over an element",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null}
                });

        // Form interaction
        addTool(tools, "send_keys", "sends keys to an element, aka typing",
                new String[]{"by", "value", "text"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null},
                        {"text", "string", "Text to enter into the element", null},
                        {"clear", "boolean", "Clear field before typing (default: true)", null},
                        {"timeout", "number", "Maximum time to wait for element in milliseconds", null}
                });

        addTool(tools, "select", "select an option from a dropdown",
                new String[]{"by", "value", "option"},
                new Object[][]{
                        {"by", "string", "Locator strategy for select element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null},
                        {"option", "string", "Option to select", null},
                        {"selectBy", "string", "How to select (value, text, or index) (default: text)", null}
                });

        // Element state
        addTool(tools, "get_element_text", "gets the text() of an element",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null},
                        {"timeout", "number", "Maximum time to wait for element in milliseconds", null}
                });

        addTool(tools, "getAttribute", "get attribute value from an element",
                new String[]{"by", "value", "attribute"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null},
                        {"attribute", "string", "Attribute name", null}
                });

        addTool(tools, "isVisible", "check if element is visible",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null}
                });

        addTool(tools, "isEnabled", "check if element is enabled",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null}
                });

        addTool(tools, "isSelected", "check if element is selected",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null}
                });

        // Screenshots
        addTool(tools, "take_screenshot", "captures a screenshot of the current page",
                new String[]{},
                new Object[][]{
                        {"outputPath", "string", "Optional path where to save the screenshot. If not provided, returns base64 data.", null}
                });

        addTool(tools, "elementScreenshot", "take a screenshot of a specific element",
                new String[]{"by", "value"},
                new Object[][]{
                        {"by", "string", "Locator strategy to find element", new String[]{"id", "css", "xpath", "name", "tag", "class"}},
                        {"value", "string", "Value for the locator strategy", null}
                });

        addTool(tools, "fullPageScreenshot", "take a full page screenshot",
                new String[]{},
                new Object[][]{});

        // Script execution
        addTool(tools, "executeScript", "execute JavaScript in the browser",
                new String[]{"script"},
                new Object[][]{
                        {"script", "string", "JavaScript code to execute", null}
                });

        addTool(tools, "executeAsyncScript", "execute async JavaScript in the browser",
                new String[]{"script"},
                new Object[][]{
                        {"script", "string", "JavaScript code to execute", null}
                });

        addTool(tools, "evaluateXPath", "evaluate XPath expression",
                new String[]{"xpath"},
                new Object[][]{
                        {"xpath", "string", "XPath expression", null}
                });

        // Page info
        addTool(tools, "getPageSource", "get the page HTML source",
                new String[]{},
                new Object[][]{});

        addTool(tools, "getPageInfo", "get page information",
                new String[]{},
                new Object[][]{});

        // Cookies
        addTool(tools, "getCookie", "get cookie value",
                new String[]{},
                new Object[][]{
                        {"name", "string", "Cookie name (optional, returns all if not specified)", null}
                });

        addTool(tools, "setCookie", "set a cookie",
                new String[]{"name", "value"},
                new Object[][]{
                        {"name", "string", "Cookie name", null},
                        {"value", "string", "Cookie value", null},
                        {"days", "number", "Days until expiration (default: 7)", null}
                });

        // Local Storage
        addTool(tools, "getLocalStorage", "get localStorage value",
                new String[]{},
                new Object[][]{
                        {"key", "string", "Storage key (optional, returns all if not specified)", null}
                });

        addTool(tools, "setLocalStorage", "set localStorage value",
                new String[]{"key", "value"},
                new Object[][]{
                        {"key", "string", "Storage key", null},
                        {"value", "string", "Storage value", null}
                });

        // Scrolling
        addTool(tools, "scrollTo", "scroll to specific position",
                new String[]{"x", "y"},
                new Object[][]{
                        {"x", "number", "X coordinate", null},
                        {"y", "number", "Y coordinate", null}
                });

        addTool(tools, "scrollBy", "scroll by relative amount",
                new String[]{"x", "y"},
                new Object[][]{
                        {"x", "number", "X offset", null},
                        {"y", "number", "Y offset", null}
                });

        // Console
        addTool(tools, "getConsoleLog", "get browser console logs",
                new String[]{},
                new Object[][]{});

        // Session
        addTool(tools, "close_session", "closes the current browser session",
                new String[]{},
                new Object[][]{});

        ObjectNode result = objectMapper.createObjectNode();
        result.set("tools", tools);
        response.set("result", result);
    }

    /**
     * Adds a tool definition to the tools array with complete JSON schema.
     * <p>
     * Creates a properly formatted tool definition including input schema,
     * parameter validation, and documentation. Follows JSON Schema Draft 7
     * specification for maximum compatibility.
     * 
     * @param tools the array to add the tool definition to
     * @param name the tool name (must be unique)
     * @param description human-readable tool description
     * @param required array of required parameter names
     * @param properties 2D array of property definitions [name, type, description, enum]
     */
    private static void addTool(ArrayNode tools, String name, String description,
                                String[] required, Object[][] properties) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("name", name);
        tool.put("description", description);

        ObjectNode inputSchema = objectMapper.createObjectNode();
        inputSchema.put("type", "object");

        ObjectNode props = objectMapper.createObjectNode();
        if (properties != null) {
            for (Object[] prop : properties) {
                String propName = (String) prop[0];
                String propType = (String) prop[1];
                String propDesc = (String) prop[2];
                String[] enumValues = (String[]) prop[3];

                ObjectNode propSchema = objectMapper.createObjectNode();
                propSchema.put("type", propType);
                propSchema.put("description", propDesc);

                if (enumValues != null) {
                    ArrayNode enumArray = objectMapper.createArrayNode();
                    for (String enumValue : enumValues) {
                        enumArray.add(enumValue);
                    }
                    propSchema.set("enum", enumArray);
                }

                props.set(propName, propSchema);
            }
        }
        inputSchema.set("properties", props);

        if (required != null && required.length > 0) {
            ArrayNode requiredArray = objectMapper.createArrayNode();
            for (String req : required) {
                requiredArray.add(req);
            }
            inputSchema.set("required", requiredArray);
        }

        inputSchema.put("additionalProperties", false);
        inputSchema.put("$schema", "http://json-schema.org/draft-07/schema#");

        tool.set("inputSchema", inputSchema);
        tools.add(tool);
    }

    /**
     * Executes automation tools and formats responses.
     * 
     * Processes tool execution requests, handles parameter mapping,
     * executes the automation operation, and formats the response
     * according to MCP content specifications.
     * 
     * @param response the response object to populate
     * @param params the tool execution parameters
     */
    private static void handleToolCall(ObjectNode response, Object params) {
        try {
            ObjectNode paramsNode = (ObjectNode) params;
            String toolName = paramsNode.get("name").asText();
            ObjectNode arguments = paramsNode.has("arguments") ?
                    (ObjectNode) paramsNode.get("arguments") : objectMapper.createObjectNode();

            System.err.println("Executing tool: " + toolName);

            // Map tool names to our implementation
            String mappedToolName = mapToolName(toolName);

            Object result = seleniumMCP.executeTool(mappedToolName, arguments);

            ArrayNode content = objectMapper.createArrayNode();

            if (result instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> resultMap = (java.util.Map<String, Object>) result;

                if ("screenshot".equals(resultMap.get("type"))) {
                    ObjectNode imageContent = objectMapper.createObjectNode();
                    imageContent.put("type", "image");
                    imageContent.put("data", resultMap.get("data").toString());
                    imageContent.put("mimeType", "image/png");
                    content.add(imageContent);
                } else {
                    ObjectNode textContent = objectMapper.createObjectNode();
                    textContent.put("type", "text");
                    textContent.put("text", objectMapper.writeValueAsString(resultMap));
                    content.add(textContent);
                }
            } else {
                ObjectNode textContent = objectMapper.createObjectNode();
                textContent.put("type", "text");
                textContent.put("text", result != null ? result.toString() : "Success");
                content.add(textContent);
            }

            ObjectNode resultNode = objectMapper.createObjectNode();
            resultNode.set("content", content);
            response.set("result", resultNode);

        } catch (Exception e) {
            System.err.println("Tool execution error: " + e.getMessage());
            e.printStackTrace(System.err);

            ObjectNode error = objectMapper.createObjectNode();
            error.put("code", -32603);
            error.put("message", "Tool execution failed: " + e.getMessage());
            response.set("error", error);
        }
    }

    /**
     * Maps external tool names to internal implementation names.
     * 
     * Provides backward compatibility and standardized naming
     * across different MCP client implementations.
     * 
     * @param toolName the external tool name
     * @return the internal tool name
     */
    private static String mapToolName(String toolName) {
        switch (toolName) {
            case "start_browser":
                return "start_browser"; // No mapear a navigate
            case "click_element":
                return "click";
            case "send_keys":
                return "fill";
            case "get_element_text":
                return "getText";
            case "take_screenshot":
                return "screenshot";
            case "close_session":
                return "cleanup";
            default:
                return toolName;
        }
    }
}