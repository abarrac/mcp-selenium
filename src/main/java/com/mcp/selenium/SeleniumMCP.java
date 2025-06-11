package com.mcp.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcp.selenium.handlers.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP Selenium - Professional WebDriver automation engine for AI agents.
 * <p>
 * This class serves as the main orchestrator for all browser automation operations,
 * providing a comprehensive suite of tools for web interaction, testing, and data extraction.
 * Designed for enterprise-grade reliability and performance.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Lazy initialization for optimal resource management</li>
 *   <li>Modular handler architecture for maintainability</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Support for headless and headed browser modes</li>
 *   <li>Cross-platform compatibility</li>
 * </ul>
 * 
 * <p>Architecture:
 * The class follows a command pattern with specialized handlers for different
 * aspects of browser automation:
 * <ul>
 *   <li>{@link NavigationHandler} - Page navigation and browser controls</li>
 *   <li>{@link ElementHandler} - DOM element interactions and queries</li>
 *   <li>{@link ScreenshotHandler} - Visual capture and documentation</li>
 *   <li>{@link ScriptHandler} - JavaScript execution and data extraction</li>
 * </ul>
 * 
 * @author Alberto Barragán
 * @version 1.0.0
 * @since 1.0.0
 */
public class SeleniumMCP {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumMCP.class);
    
    /** WebDriver instance - initialized lazily for optimal resource usage */
    private WebDriver driver;
    
    /** JSON object mapper for request/response processing */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Specialized handlers for different automation aspects */
    private NavigationHandler navigationHandler;
    private ElementHandler elementHandler;
    private ScreenshotHandler screenshotHandler;
    private ScriptHandler scriptHandler;

    /**
     * Constructs a new SeleniumMCP instance with lazy initialization.
     * <p>
     * The WebDriver is not initialized immediately to conserve system resources
     * and allow for configuration flexibility. Browser initialization occurs
     * on the first operation that requires it.
     */
    public SeleniumMCP() {
        // Lazy initialization pattern - driver initialized on first use
    }

    /**
     * Initializes the WebDriver with enterprise-grade configuration.
     * <p>
     * Configures Chrome with optimal settings for automation, including:
     * <ul>
     *   <li>Security bypasses for automation environments</li>
     *   <li>Performance optimizations</li>
     *   <li>Anti-detection measures</li>
     *   <li>Headless mode support via environment variables</li>
     * </ul>
     * 
     * @throws RuntimeException if driver initialization fails
     */
    private void initializeDriver() {
        try {
            // Automatic driver management - no manual setup required
            WebDriverManager.chromedriver().setup();

            // Enterprise-grade Chrome configuration
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");                              // Docker/CI compatibility
            options.addArguments("--disable-dev-shm-usage");                   // Memory optimization
            options.addArguments("--disable-blink-features=AutomationControlled"); // Anti-detection
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            // Environment-based headless mode configuration
            String headless = System.getenv("SELENIUM_HEADLESS");
            if ("true".equals(headless)) {
                options.addArguments("--headless");
                logger.info("Running in headless mode");
            }

            driver = new ChromeDriver(options);
            logger.info("WebDriver initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing WebDriver", e);
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }

    /**
     * Initializes all automation handlers with the active WebDriver instance.
     * <p>
     * Creates specialized handler instances for different automation domains,
     * ensuring proper dependency injection and consistent driver usage.
     */
    private void initializeHandlers() {
        navigationHandler = new NavigationHandler(driver);
        elementHandler = new ElementHandler(driver);
        screenshotHandler = new ScreenshotHandler(driver);
        scriptHandler = new ScriptHandler(driver);
        logger.debug("All handlers initialized successfully");
    }

    /**
     * Executes a browser automation tool with the specified arguments.
     * <p>
     * This is the main entry point for all automation operations. The method
     * implements a command pattern, routing tool requests to appropriate handlers
     * based on the tool name. Supports lazy initialization and comprehensive
     * error handling.
     * 
     * <p>Supported tool categories:
     * <ul>
     *   <li><strong>Browser Control:</strong> start_browser, close_session</li>
     *   <li><strong>Navigation:</strong> navigate, goBack, goForward, refresh</li>
     *   <li><strong>Element Interaction:</strong> click, fill, hover, select</li>
     *   <li><strong>Element Queries:</strong> find_element, getText, getAttribute</li>
     *   <li><strong>Screenshots:</strong> take_screenshot, elementScreenshot</li>
     *   <li><strong>Script Execution:</strong> executeScript, evaluateXPath</li>
     *   <li><strong>State Management:</strong> cookies, localStorage, scrolling</li>
     * </ul>
     * 
     * @param toolName the name of the automation tool to execute
     * @param arguments JSON object containing tool-specific parameters
     * @return tool execution result, format varies by tool type
     * @throws IllegalArgumentException if the tool name is not recognized
     * @throws RuntimeException if tool execution fails
     */
    public Object executeTool(String toolName, ObjectNode arguments) {
        try {
            switch (toolName) {
                case "find_element":
                    // Just find and return info about element
                    if (driver == null) {
                        return "No browser session active";
                    }
                    String findBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String findValue = arguments.get("value").asText();
                    boolean found = elementHandler.isVisible(convertSelector(findBy, findValue));
                    return found ? "Element found" : "Element not found";

                case "start_browser":
                    // Browser is started automatically on first navigation
                    if (driver == null) {
                        initializeDriver();
                        initializeHandlers();
                    }
                    return "Browser started successfully";

                // Navigation tools
                case "navigate":
                    if (driver == null) {
                        initializeDriver();
                        initializeHandlers();
                    }
                    return navigationHandler.navigate(arguments.get("url").asText());
                case "goBack":
                    if (driver == null) return "No browser session active";
                    return navigationHandler.goBack();
                case "goForward":
                    if (driver == null) return "No browser session active";
                    return navigationHandler.goForward();
                case "refresh":
                    if (driver == null) return "No browser session active";
                    return navigationHandler.refresh();
                case "getUrl":
                case "getCurrentUrl":
                    return driver != null ? navigationHandler.getCurrentUrl() : "No browser session active";
                case "getTitle":
                    return driver != null ? navigationHandler.getTitle() : "No browser session active";

                // Element interaction tools
                case "click":
                case "click_element":
                    if (driver == null) return "No browser session active";
                    String by = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String value = arguments.get("value").asText();
                    return elementHandler.click(convertSelector(by, value));
                case "fill":
                case "send_keys":
                    if (driver == null) return "No browser session active";
                    String fillBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String fillValue = arguments.get("value").asText();
                    String text = arguments.has("text") ? arguments.get("text").asText() : arguments.get("value").asText();
                    boolean clear = arguments.has("clear") ? arguments.get("clear").asBoolean() : true;
                    return elementHandler.fill(
                            convertSelector(fillBy, fillValue),
                            text,
                            clear
                    );
                case "doubleClick":
                    if (driver == null) return "No browser session active";
                    String doubleBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String doubleValue = arguments.get("value").asText();
                    return elementHandler.doubleClick(convertSelector(doubleBy, doubleValue));
                case "rightClick":
                    if (driver == null) return "No browser session active";
                    String rightBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String rightValue = arguments.get("value").asText();
                    return elementHandler.rightClick(convertSelector(rightBy, rightValue));
                case "hover":
                    if (driver == null) return "No browser session active";
                    String hoverBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String hoverValue = arguments.get("value").asText();
                    return elementHandler.hover(convertSelector(hoverBy, hoverValue));
                case "select":
                    if (driver == null) return "No browser session active";
                    String selectBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String selectValue = arguments.get("value").asText();
                    String option = arguments.get("option").asText();
                    String selectByType = arguments.has("selectBy") ? arguments.get("selectBy").asText() : "text";
                    return elementHandler.select(
                            convertSelector(selectBy, selectValue),
                            option,
                            selectByType
                    );

                // State checking tools
                case "getText":
                case "get_element_text":
                    if (driver == null) return "No browser session active";
                    String textBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String textValue = arguments.get("value").asText();
                    return elementHandler.getText(convertSelector(textBy, textValue));
                case "getAttribute":
                    if (driver == null) return "No browser session active";
                    String attrBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String attrValue = arguments.get("value").asText();
                    return elementHandler.getAttribute(
                            convertSelector(attrBy, attrValue),
                            arguments.get("attribute").asText()
                    );
                case "isVisible":
                    if (driver == null) return "No browser session active";
                    String visBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String visValue = arguments.get("value").asText();
                    return elementHandler.isVisible(convertSelector(visBy, visValue));
                case "isEnabled":
                    if (driver == null) return "No browser session active";
                    String enBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String enValue = arguments.get("value").asText();
                    return elementHandler.isEnabled(convertSelector(enBy, enValue));
                case "isSelected":
                    if (driver == null) return "No browser session active";
                    String selBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String selValue = arguments.get("value").asText();
                    return elementHandler.isSelected(convertSelector(selBy, selValue));
                case "findElements":
                    if (driver == null) return "No browser session active";
                    String feBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String feValue = arguments.get("value").asText();
                    return elementHandler.findElements(convertSelector(feBy, feValue));
                case "waitForElement":
                    if (driver == null) return "No browser session active";
                    String waitBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String waitValue = arguments.get("value").asText();
                    int timeout = arguments.has("timeout") ? arguments.get("timeout").asInt() : 10;
                    return elementHandler.waitForElement(convertSelector(waitBy, waitValue), timeout);

                // Screenshot tools
                case "screenshot":
                case "take_screenshot":
                    if (driver == null) return "No browser session active";
                    return screenshotHandler.takeScreenshot();
                case "elementScreenshot":
                    if (driver == null) return "No browser session active";
                    String esBy = arguments.has("by") ? arguments.get("by").asText() : "css";
                    String esValue = arguments.get("value").asText();
                    return screenshotHandler.takeElementScreenshot(convertSelector(esBy, esValue));
                case "fullPageScreenshot":
                    if (driver == null) return "No browser session active";
                    return screenshotHandler.takeFullPageScreenshot();

                // Script execution tools
                case "executeScript":
                    if (driver == null) return "No browser session active";
                    return scriptHandler.executeScript(arguments.get("script").asText());
                case "executeAsyncScript":
                    if (driver == null) return "No browser session active";
                    return scriptHandler.executeAsyncScript(arguments.get("script").asText());
                case "evaluateXPath":
                    if (driver == null) return "No browser session active";
                    return scriptHandler.evaluateXPath(arguments.get("xpath").asText());
                case "getPageSource":
                    if (driver == null) return "No browser session active";
                    return scriptHandler.getPageSource();
                case "getPageInfo":
                    if (driver == null) return "No browser session active";
                    return scriptHandler.getPageInfo();

                // Cookie management
                case "getCookie":
                    if (driver == null) return "No browser session active";
                    String cookieName = arguments.has("name") ? arguments.get("name").asText() : null;
                    return scriptHandler.getCookie(cookieName);
                case "setCookie":
                    if (driver == null) return "No browser session active";
                    int days = arguments.has("days") ? arguments.get("days").asInt() : 7;
                    return scriptHandler.setCookie(
                            arguments.get("name").asText(),
                            arguments.get("value").asText(),
                            days
                    );

                // Local storage
                case "getLocalStorage":
                    if (driver == null) return "No browser session active";
                    String key = arguments.has("key") ? arguments.get("key").asText() : null;
                    return scriptHandler.getLocalStorage(key);
                case "setLocalStorage":
                    if (driver == null) return "No browser session active";
                    return scriptHandler.setLocalStorage(
                            arguments.get("key").asText(),
                            arguments.get("value").asText()
                    );

                // Scrolling
                case "scrollTo":
                    if (driver == null) return "No browser session active";
                    return scriptHandler.scrollTo(
                            arguments.get("x").asInt(),
                            arguments.get("y").asInt()
                    );
                case "scrollBy":
                    if (driver == null) return "No browser session active";
                    return scriptHandler.scrollBy(
                            arguments.get("x").asInt(),
                            arguments.get("y").asInt()
                    );

                // Console logs
                case "getConsoleLog":
                    if (driver == null) return "No browser session active";
                    return scriptHandler.getConsoleLog();

                // Session management
                case "cleanup":
                case "close_session":
                    cleanup();
                    return "Browser session closed";

                default:
                    throw new IllegalArgumentException("Unknown tool: " + toolName);
            }
        } catch (Exception e) {
            logger.error("Error executing tool: {}", toolName, e);
            return createErrorResponse(e.getMessage());
        }
    }

    /**
     * Converts MCP selector format to internal handler format.
     * 
     * Transforms the standardized MCP selector format into the internal
     * format used by element handlers. Supports all major selector types
     * used in modern web automation.
     * 
     * @param by the selector strategy (id, class, xpath, name, tag, css)
     * @param value the selector value
     * @return formatted selector string for internal use
     */
    private String convertSelector(String by, String value) {
        switch (by.toLowerCase()) {
            case "id":
                return "id=" + value;
            case "class":
                return "class=" + value;
            case "xpath":
                return value;
            case "name":
                return "name=" + value;
            case "tag":
                return "tag=" + value;
            case "css":
            default:
                return value;
        }
    }

    /**
     * Creates a standardized error response object.
     * 
     * Provides consistent error formatting across all tool operations,
     * ensuring uniform error handling for client applications.
     * 
     * @param message the error message to include in the response
     * @return standardized error response map
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    /**
     * Performs cleanup operations and releases all resources.
     * 
     * Gracefully shuts down the WebDriver instance and releases associated
     * system resources. Should be called when automation session is complete
     * or when the application is shutting down.
     * 
     * <p>This method is safe to call multiple times and handles cleanup
     * errors gracefully without throwing exceptions.
     */
    public void cleanup() {
        if (driver != null) {
            try {
                driver.quit();
                driver = null;
                logger.info("WebDriver closed successfully");
            } catch (Exception e) {
                logger.error("Error closing WebDriver", e);
            }
        }
    }
}