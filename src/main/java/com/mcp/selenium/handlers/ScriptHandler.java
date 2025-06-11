package com.mcp.selenium.handlers;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * ScriptHandler - Advanced JavaScript execution and page interaction engine.
 * 
 * Provides comprehensive JavaScript execution capabilities for complex browser
 * automation, data extraction, and dynamic content manipulation. Supports both
 * synchronous and asynchronous script execution with intelligent result formatting.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Synchronous and asynchronous JavaScript execution</li>
 *   <li>XPath evaluation with detailed result analysis</li>
 *   <li>Page information extraction and DOM querying</li>
 *   <li>Cookie and localStorage management</li>
 *   <li>Scroll control and viewport manipulation</li>
 *   <li>Console log capture and monitoring</li>
 *   <li>Intelligent result formatting and type handling</li>
 * </ul>
 * 
 * <p>Script Safety:
 * All JavaScript execution includes proper error handling and timeout protection.
 * Results are intelligently formatted to handle WebElement objects, collections,
 * and complex data structures returned from browser context.
 * 
 * @author Alberto Barragán
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScriptHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScriptHandler.class);
    
    /** WebDriver instance for browser control */
    private final WebDriver driver;
    
    /** JavascriptExecutor instance for script execution */
    private final JavascriptExecutor js;

    /**
     * Constructs a new ScriptHandler with the specified WebDriver.
     * 
     * Initializes the handler with both WebDriver and JavascriptExecutor
     * interfaces for comprehensive script execution capabilities.
     * 
     * @param driver the WebDriver instance to use for script execution
     */
    public ScriptHandler(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
    }

    /**
     * Executes synchronous JavaScript code in the browser context.
     * 
     * Runs the provided JavaScript code and returns the formatted result.
     * Supports parameter passing and handles various return types including
     * WebElements, collections, and primitive values.
     * 
     * @param script the JavaScript code to execute
     * @param args optional arguments to pass to the script
     * @return formatted execution result (type varies based on script output)
     * @throws RuntimeException if script execution fails
     */
    public Object executeScript(String script, Object... args) {
        try {
            Object result = js.executeScript(script, args);
            return formatResult(result);
        } catch (Exception e) {
            logger.error("Failed to execute script", e);
            throw new RuntimeException("Script execution failed: " + e.getMessage());
        }
    }

    /**
     * Executes asynchronous JavaScript code in the browser context.
     * 
     * Runs JavaScript code that requires asynchronous execution patterns
     * such as callbacks, promises, or timed operations. The script should
     * call the provided callback function when complete.
     * 
     * @param script the asynchronous JavaScript code to execute
     * @param args optional arguments to pass to the script
     * @return formatted execution result when async operation completes
     * @throws RuntimeException if async script execution fails
     */
    public Object executeAsyncScript(String script, Object... args) {
        try {
            Object result = js.executeAsyncScript(script, args);
            return formatResult(result);
        } catch (Exception e) {
            logger.error("Failed to execute async script", e);
            throw new RuntimeException("Async script execution failed: " + e.getMessage());
        }
    }

    /**
     * Evaluates XPath expressions and returns detailed node information.
     * 
     * Executes XPath queries against the current document and returns
     * comprehensive information about matching nodes including tag names,
     * text content, IDs, and class names.
     * 
     * @param xpath the XPath expression to evaluate
     * @return detailed evaluation result map containing:
     *         - xpath: the original XPath expression
     *         - count: number of matching nodes
     *         - elements: array of node information objects
     * @throws RuntimeException if XPath evaluation fails
     */
    public Map<String, Object> evaluateXPath(String xpath) {
        try {
            String script =
                    "var result = document.evaluate(arguments[0], document, null, " +
                            "XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);" +
                            "var nodes = [];" +
                            "for (var i = 0; i < result.snapshotLength; i++) {" +
                            "  var node = result.snapshotItem(i);" +
                            "  nodes.push({" +
                            "    tagName: node.tagName," +
                            "    text: node.textContent," +
                            "    id: node.id," +
                            "    className: node.className" +
                            "  });" +
                            "}" +
                            "return nodes;";

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) js.executeScript(script, xpath);

            Map<String, Object> result = new HashMap<>();
            result.put("xpath", xpath);
            result.put("count", nodes.size());
            result.put("elements", nodes);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("XPath evaluation failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves the complete HTML source of the current page.
     * 
     * Returns the full HTML document source including all dynamically
     * generated content and modifications made by JavaScript.
     * 
     * @return the complete HTML source code as a string
     */
    public String getPageSource() {
        return driver.getPageSource();
    }

    /**
     * Gathers comprehensive information about the current page state.
     * 
     * Collects detailed page metadata including URL, title, document ready
     * state, and scroll dimensions. Useful for page analysis and debugging.
     * 
     * @return detailed page information map containing:
     *         - url: current page URL
     *         - title: page title
     *         - readyState: document ready state
     *         - documentElement: scroll and client dimensions
     * @throws RuntimeException if page information gathering fails
     */
    public Map<String, Object> getPageInfo() {
        try {
            Map<String, Object> info = new HashMap<>();

            info.put("url", driver.getCurrentUrl());
            info.put("title", driver.getTitle());
            info.put("readyState", js.executeScript("return document.readyState"));
            info.put("documentElement", js.executeScript(
                    "return {" +
                            "  scrollHeight: document.documentElement.scrollHeight," +
                            "  scrollWidth: document.documentElement.scrollWidth," +
                            "  clientHeight: document.documentElement.clientHeight," +
                            "  clientWidth: document.documentElement.clientWidth" +
                            "}"
            ));

            return info;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get page info: " + e.getMessage());
        }
    }

    public Object getCookie(String name) {
        try {
            if (name == null) {
                return js.executeScript("return document.cookie");
            } else {
                String script =
                        "var name = arguments[0] + '=';" +
                                "var decodedCookie = decodeURIComponent(document.cookie);" +
                                "var ca = decodedCookie.split(';');" +
                                "for(var i = 0; i < ca.length; i++) {" +
                                "  var c = ca[i];" +
                                "  while (c.charAt(0) == ' ') {" +
                                "    c = c.substring(1);" +
                                "  }" +
                                "  if (c.indexOf(name) == 0) {" +
                                "    return c.substring(name.length, c.length);" +
                                "  }" +
                                "}" +
                                "return null;";
                return js.executeScript(script, name);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get cookie: " + e.getMessage());
        }
    }

    /**
     * Sets a browser cookie with the specified name, value, and expiration.
     * 
     * Creates or updates a cookie using client-side JavaScript with automatic
     * expiration date calculation based on the provided days parameter.
     * 
     * @param name the cookie name
     * @param value the cookie value
     * @param days number of days until expiration
     * @return success message confirming cookie creation
     * @throws RuntimeException if cookie setting fails
     */
    public String setCookie(String name, String value, int days) {
        try {
            String script =
                    "var d = new Date();" +
                            "d.setTime(d.getTime() + (arguments[2] * 24 * 60 * 60 * 1000));" +
                            "var expires = 'expires=' + d.toUTCString();" +
                            "document.cookie = arguments[0] + '=' + arguments[1] + ';' + expires + ';path=/';";

            js.executeScript(script, name, value, days);
            return "Cookie set: " + name;
        } catch (Exception e) {
            throw new RuntimeException("Failed to set cookie: " + e.getMessage());
        }
    }

    /**
     * Retrieves localStorage data with optional key filtering.
     * 
     * Returns either all localStorage items as a key-value map or the value
     * of a specific key if provided. Handles localStorage access with proper
     * error handling for browsers that may have localStorage disabled.
     * 
     * @param key optional localStorage key to retrieve (null returns all items)
     * @return localStorage value or complete localStorage map if key is null
     * @throws RuntimeException if localStorage access fails
     */
    public Object getLocalStorage(String key) {
        try {
            if (key == null) {
                return js.executeScript(
                        "var items = {};" +
                                "for (var i = 0; i < localStorage.length; i++) {" +
                                "  var key = localStorage.key(i);" +
                                "  items[key] = localStorage.getItem(key);" +
                                "}" +
                                "return items;"
                );
            } else {
                return js.executeScript("return localStorage.getItem(arguments[0]);", key);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get localStorage: " + e.getMessage());
        }
    }

    /**
     * Sets a localStorage item with the specified key and value.
     * 
     * Stores data in the browser's localStorage using client-side JavaScript.
     * Data persists until explicitly removed or browser storage is cleared.
     * 
     * @param key the localStorage key
     * @param value the value to store
     * @return success message confirming storage operation
     * @throws RuntimeException if localStorage write fails
     */
    public String setLocalStorage(String key, String value) {
        try {
            js.executeScript("localStorage.setItem(arguments[0], arguments[1]);", key, value);
            return "localStorage set: " + key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to set localStorage: " + e.getMessage());
        }
    }

    /**
     * Scrolls the browser window to the specified absolute coordinates.
     * 
     * Moves the viewport to the exact position specified by x and y coordinates.
     * Uses smooth scrolling when supported by the browser.
     * 
     * @param x the horizontal scroll position in pixels
     * @param y the vertical scroll position in pixels
     * @return success message with the scroll coordinates
     * @throws RuntimeException if scroll operation fails
     */
    public String scrollTo(int x, int y) {
        try {
            js.executeScript("window.scrollTo(arguments[0], arguments[1]);", x, y);
            return String.format("Scrolled to position: %d, %d", x, y);
        } catch (Exception e) {
            throw new RuntimeException("Failed to scroll: " + e.getMessage());
        }
    }

    /**
     * Scrolls the browser window by the specified relative amounts.
     * 
     * Moves the viewport relative to its current position by the specified
     * x and y offsets. Positive values scroll right/down, negative values
     * scroll left/up.
     * 
     * @param x the horizontal scroll offset in pixels
     * @param y the vertical scroll offset in pixels
     * @return success message with the scroll offsets
     * @throws RuntimeException if scroll operation fails
     */
    public String scrollBy(int x, int y) {
        try {
            js.executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
            return String.format("Scrolled by: %d, %d", x, y);
        } catch (Exception e) {
            throw new RuntimeException("Failed to scroll: " + e.getMessage());
        }
    }

    /**
     * Captures and returns browser console logs with automatic interception.
     * 
     * Sets up console log interception if not already active and returns
     * all captured log entries. The interception captures console.log calls
     * with timestamps and message content.
     * 
     * @return detailed console log information map containing:
     *         - logs: array of log entry objects with type, message, and timestamp
     *         - count: total number of log entries captured
     * @throws RuntimeException if console log retrieval fails
     */
    public Map<String, Object> getConsoleLog() {
        try {
            String script =
                    "var logs = window.__consoleLogs || [];" +
                            "window.__consoleLogs = [];" +
                            "return logs;";

            // Setup console interception if not already configured
            js.executeScript(
                    "if (!window.__consoleIntercepted) {" +
                            "  window.__consoleLogs = [];" +
                            "  var oldLog = console.log;" +
                            "  console.log = function() {" +
                            "    window.__consoleLogs.push({" +
                            "      type: 'log'," +
                            "      message: Array.from(arguments).join(' ')," +
                            "      timestamp: new Date().toISOString()" +
                            "    });" +
                            "    oldLog.apply(console, arguments);" +
                            "  };" +
                            "  window.__consoleIntercepted = true;" +
                            "}"
            );

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> logs = (List<Map<String, Object>>) js.executeScript(script);

            Map<String, Object> result = new HashMap<>();
            result.put("logs", logs);
            result.put("count", logs.size());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get console logs: " + e.getMessage());
        }
    }

    /**
     * Intelligently formats JavaScript execution results for consistent output.
     * 
     * Handles various result types returned from JavaScript execution including
     * WebElements, collections, and primitive values. Provides consistent
     * formatting for complex objects and maintains type safety.
     * 
     * @param result the raw result from JavaScript execution
     * @return formatted result object with consistent structure
     */
    private Object formatResult(Object result) {
        if (result == null) {
            return "null";
        } else if (result instanceof WebElement) {
            // Convert WebElement to informational map
            WebElement element = (WebElement) result;
            Map<String, String> elementInfo = new HashMap<>();
            elementInfo.put("tagName", element.getTagName());
            elementInfo.put("text", element.getText());
            elementInfo.put("displayed", String.valueOf(element.isDisplayed()));
            return elementInfo;
        } else if (result instanceof List) {
            // Recursively format list elements
            List<?> list = (List<?>) result;
            List<Object> formatted = new ArrayList<>();
            for (Object item : list) {
                formatted.add(formatResult(item));
            }
            return formatted;
        } else {
            // Return primitive types and objects as strings
            return result.toString();
        }
    }
}