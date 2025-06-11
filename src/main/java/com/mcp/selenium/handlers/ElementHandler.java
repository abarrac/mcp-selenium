package com.mcp.selenium.handlers;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ElementHandler - Advanced DOM element interaction and manipulation.
 * 
 * Provides comprehensive element interaction capabilities including clicking,
 * typing, form manipulation, and state querying. Implements intelligent
 * element waiting, scrolling, and error recovery mechanisms.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Smart element waiting with configurable timeouts</li>
 *   <li>Automatic scrolling for element visibility</li>
 *   <li>Advanced interaction methods (click, hover, drag)</li>
 *   <li>Form handling with input validation</li>
 *   <li>Element state querying and attribute extraction</li>
 *   <li>Robust error handling with fallback strategies</li>
 * </ul>
 * 
 * <p>Selector Support:
 * Supports all major selector types: CSS, XPath, ID, Name, Class, Tag,
 * Link Text, and Partial Link Text. Automatic selector type detection
 * based on format patterns.
 * 
 * @author Alberto Barragán
 * @version 1.0.0
 * @since 1.0.0
 */
public class ElementHandler {
    private static final Logger logger = LoggerFactory.getLogger(ElementHandler.class);
    
    /** WebDriver instance for browser automation */
    private final WebDriver driver;
    
    /** WebDriverWait instance for intelligent element waiting */
    private final WebDriverWait wait;
    
    /** Actions instance for advanced user interactions */
    private final Actions actions;

    /**
     * Constructs a new ElementHandler with the specified WebDriver.
     * 
     * Initializes the handler with a default wait timeout of 10 seconds
     * and prepares the Actions instance for advanced interactions.
     * 
     * @param driver the WebDriver instance to use for element operations
     */
    public ElementHandler(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.actions = new Actions(driver);
    }

    /**
     * Performs a click operation on the specified element.
     * 
     * Locates the element, ensures it's visible and clickable, then performs
     * the click action. Includes automatic scrolling and wait conditions for
     * reliable interaction across different page layouts.
     * 
     * @param selector the element selector (supports all selector types)
     * @return success message indicating the click was performed
     * @throws RuntimeException if the element cannot be found or clicked
     */
    public String click(String selector) {
        try {
            WebElement element = findElement(selector);
            scrollToElement(element);
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            return "Clicked element: " + selector;
        } catch (Exception e) {
            logger.error("Click failed", e);
            throw new RuntimeException("Failed to click element: " + e.getMessage());
        }
    }

    /**
     * Performs a double-click operation on the specified element.
     * 
     * Uses the Actions API to simulate a precise double-click gesture.
     * Ensures element visibility before performing the action.
     * 
     * @param selector the element selector to double-click
     * @return success message indicating the double-click was performed
     * @throws RuntimeException if the element cannot be found or double-clicked
     */
    public String doubleClick(String selector) {
        try {
            WebElement element = findElement(selector);
            scrollToElement(element);
            actions.doubleClick(element).perform();
            return "Double-clicked element: " + selector;
        } catch (Exception e) {
            throw new RuntimeException("Failed to double-click element: " + e.getMessage());
        }
    }

    /**
     * Performs a right-click (context menu) operation on the specified element.
     * 
     * Uses the Actions API to simulate a right-click gesture, typically
     * used to open context menus or trigger custom interactions.
     * 
     * @param selector the element selector to right-click
     * @return success message indicating the right-click was performed
     * @throws RuntimeException if the element cannot be found or right-clicked
     */
    public String rightClick(String selector) {
        try {
            WebElement element = findElement(selector);
            scrollToElement(element);
            actions.contextClick(element).perform();
            return "Right-clicked element: " + selector;
        } catch (Exception e) {
            throw new RuntimeException("Failed to right-click element: " + e.getMessage());
        }
    }

    /**
     * Performs a hover operation over the specified element.
     * 
     * Simulates mouse hovering to trigger hover effects, tooltips, or
     * dropdown menus. Includes fallback JavaScript-based hover for
     * elements that don't respond to Actions hover.
     * 
     * @param selector the element selector to hover over
     * @return success message indicating the hover was performed
     * @throws RuntimeException if the element cannot be found or hovered
     */
    public String hover(String selector) {
        try {
            WebElement element = findElement(selector);
            scrollToElement(element);
            Thread.sleep(1000); // Brief pause for stability
            
            try {
                actions.moveToElement(element).perform();
            } catch (Exception e) {
                // Fallback to JavaScript-based hover for stubborn elements
                ((JavascriptExecutor) driver).executeScript(
                    "var event = new MouseEvent('mouseover', {" +
                    "  'view': window," +
                    "  'bubbles': true," +
                    "  'cancelable': true" +
                    "});" +
                    "arguments[0].dispatchEvent(event);",
                    element
                );
            }
            
            return "Hovered over element: " + selector;
        } catch (Exception e) {
            throw new RuntimeException("Failed to hover over element: " + e.getMessage());
        }
    }

    /**
     * Fills an input element with the specified text value.
     * 
     * Supports various input types including text fields, textareas,
     * and other form elements. Optionally clears existing content
     * before entering new text.
     * 
     * @param selector the input element selector
     * @param value the text value to enter
     * @param clear whether to clear existing content first (recommended: true)
     * @return success message with the entered value
     * @throws RuntimeException if the element cannot be found or filled
     */
    public String fill(String selector, String value, boolean clear) {
        try {
            WebElement element = findElement(selector);
            scrollToElement(element);

            if (clear) {
                element.clear();
            }

            element.sendKeys(value);
            return String.format("Filled element %s with: %s", selector, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fill element: " + e.getMessage());
        }
    }

    /**
     * Selects an option from a dropdown/select element.
     * 
     * Supports selection by visible text, value attribute, or index position.
     * Automatically wraps the element in a Select object for proper handling.
     * 
     * @param selector the select element selector
     * @param option the option to select (text, value, or index based on 'by' parameter)
     * @param by the selection method: "text" (default), "value", or "index"
     * @return success message indicating the selection was made
     * @throws RuntimeException if the element or option cannot be found
     */
    public String select(String selector, String option, String by) {
        try {
            WebElement element = findElement(selector);
            Select select = new Select(element);

            switch (by.toLowerCase()) {
                case "value":
                    select.selectByValue(option);
                    break;
                case "text":
                    select.selectByVisibleText(option);
                    break;
                case "index":
                    select.selectByIndex(Integer.parseInt(option));
                    break;
                default:
                    select.selectByVisibleText(option); // Default to text selection
            }

            return String.format("Selected '%s' in dropdown: %s", option, selector);
        } catch (Exception e) {
            throw new RuntimeException("Failed to select option: " + e.getMessage());
        }
    }

    /**
     * Retrieves the text content of the specified element.
     * 
     * Returns the visible text content of an element. For input elements,
     * automatically retrieves the value attribute if text content is empty.
     * Handles various element types intelligently.
     * 
     * @param selector the element selector to get text from
     * @return the text content or value of the element
     * @throws RuntimeException if the element cannot be found
     */
    public String getText(String selector) {
        try {
            WebElement element = findElement(selector);
            String text = element.getText();

            if (text.isEmpty() && element.getTagName().equals("input")) {
                text = element.getAttribute("value");
            }

            return text;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get text: " + e.getMessage());
        }
    }

    /**
     * Retrieves the value of a specific attribute from an element.
     * 
     * Returns detailed information about the requested attribute including
     * the selector used, attribute name, and the actual value. Returns empty
     * string for null values to ensure consistent response format.
     * 
     * @param selector the element selector
     * @param attribute the name of the attribute to retrieve
     * @return map containing selector, attribute name, and value
     * @throws RuntimeException if the element cannot be found
     */
    public Map<String, String> getAttribute(String selector, String attribute) {
        try {
            WebElement element = findElement(selector);
            String value = element.getAttribute(attribute);

            Map<String, String> result = new HashMap<>();
            result.put("selector", selector);
            result.put("attribute", attribute);
            result.put("value", value != null ? value : ""); // Ensure non-null response

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get attribute: " + e.getMessage());
        }
    }

    /**
     * Checks if an element is currently visible on the page.
     * 
     * Determines element visibility using WebDriver's isDisplayed() method.
     * Returns false if the element cannot be found rather than throwing
     * an exception, allowing for graceful visibility checks.
     * 
     * @param selector the element selector to check
     * @return true if the element exists and is visible, false otherwise
     */
    public boolean isVisible(String selector) {
        try {
            WebElement element = findElement(selector);
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if an element is currently enabled for interaction.
     * 
     * Determines if an element can be interacted with (not disabled).
     * Useful for form validation and interactive element state checking.
     * 
     * @param selector the element selector to check
     * @return true if the element exists and is enabled, false otherwise
     */
    public boolean isEnabled(String selector) {
        try {
            WebElement element = findElement(selector);
            return element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if an element is currently selected (for checkboxes, radio buttons, options).
     * 
     * Determines the selection state of form elements that support selection.
     * Particularly useful for checkbox and radio button state validation.
     * 
     * @param selector the element selector to check
     * @return true if the element exists and is selected, false otherwise
     */
    public boolean isSelected(String selector) {
        try {
            WebElement element = findElement(selector);
            return element.isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finds multiple elements matching the specified selector.
     * 
     * Returns detailed information about all matching elements including
     * their index, text content, tag name, and visibility status.
     * Useful for element enumeration and batch operations.
     * 
     * @param selector the element selector to search for
     * @return list of element information maps
     * @throws RuntimeException if the search operation fails
     */
    public List<Map<String, String>> findElements(String selector) {
        try {
            List<WebElement> elements = driver.findElements(getBy(selector));
            List<Map<String, String>> results = new ArrayList<>();

            for (int i = 0; i < elements.size(); i++) {
                WebElement element = elements.get(i);
                Map<String, String> info = new HashMap<>();
                info.put("index", String.valueOf(i));
                info.put("text", element.getText());
                info.put("tag", element.getTagName());
                info.put("visible", String.valueOf(element.isDisplayed()));
                results.add(info);
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find elements: " + e.getMessage());
        }
    }

    /**
     * Waits for an element to appear on the page within the specified timeout.
     * 
     * Uses explicit wait conditions to wait for element presence. Configurable
     * timeout allows for flexibility in handling slow-loading elements.
     * 
     * @param selector the element selector to wait for
     * @param timeout the maximum time to wait in seconds
     * @return success message when element is found
     * @throws RuntimeException if element is not found within the timeout
     */
    public String waitForElement(String selector, int timeout) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            customWait.until(ExpectedConditions.presenceOfElementLocated(getBy(selector)));
            return "Element found: " + selector;
        } catch (Exception e) {
            throw new RuntimeException("Element not found within timeout: " + selector);
        }
    }

    /**
     * Internal method to find a single element with intelligent waiting.
     * 
     * Uses the configured WebDriverWait to ensure element presence before
     * returning the WebElement instance. This method is used internally
     * by all element interaction methods.
     * 
     * @param selector the element selector
     * @return the WebElement instance
     * @throws RuntimeException if element cannot be found
     */
    private WebElement findElement(String selector) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(getBy(selector)));
    }

    /**
     * Converts selector strings to appropriate By locator objects.
     * 
     * Supports all major selector types with automatic detection based on
     * string patterns. Provides a unified interface for different locator
     * strategies used throughout the automation framework.
     * 
     * <p>Supported selector types:
     * <ul>
     *   <li>XPath: starts with "//"</li>
     *   <li>ID: starts with "id="</li>
     *   <li>Name: starts with "name="</li>
     *   <li>Class: starts with "class="</li>
     *   <li>Tag: starts with "tag="</li>
     *   <li>Link Text: starts with "link="</li>
     *   <li>Partial Link: starts with "partial="</li>
     *   <li>CSS: default for all other patterns</li>
     * </ul>
     * 
     * @param selector the selector string to convert
     * @return appropriate By locator object
     */
    private By getBy(String selector) {
        if (selector.startsWith("//")) {
            return By.xpath(selector);
        } else if (selector.startsWith("id=")) {
            return By.id(selector.substring(3));
        } else if (selector.startsWith("name=")) {
            return By.name(selector.substring(5));
        } else if (selector.startsWith("class=")) {
            return By.className(selector.substring(6));
        } else if (selector.startsWith("tag=")) {
            return By.tagName(selector.substring(4));
        } else if (selector.startsWith("link=")) {
            return By.linkText(selector.substring(5));
        } else if (selector.startsWith("partial=")) {
            return By.partialLinkText(selector.substring(8));
        } else {
            return By.cssSelector(selector);
        }
    }

    /**
     * Intelligently scrolls an element into the viewport for optimal interaction.
     * 
     * Implements a multi-stage scrolling strategy:
     * <ol>
     *   <li>Smooth scroll to center the element in viewport</li>
     *   <li>Verify element visibility after scroll</li>
     *   <li>Apply fallback scrolling if element remains hidden</li>
     * </ol>
     * 
     * This ensures reliable element interactions across different page layouts
     * and responsive designs.
     * 
     * @param element the WebElement to scroll into view
     */
    private void scrollToElement(WebElement element) {
        try {
            // Primary scroll strategy - smooth scroll to center
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'});",
                    element
            );
            Thread.sleep(500); // Allow scroll animation to complete
            
            // Verify element is in viewport after scrolling
            Boolean isInViewport = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var rect = arguments[0].getBoundingClientRect();" +
                "return (" +
                "  rect.top >= 0 &&" +
                "  rect.left >= 0 &&" +
                "  rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&" +
                "  rect.right <= (window.innerWidth || document.documentElement.clientWidth)" +
                ");",
                element
            );
            
            if (!isInViewport) {
                // Fallback strategy - direct viewport positioning
                ((JavascriptExecutor) driver).executeScript(
                    "window.scrollTo({" +
                    "  top: arguments[0].offsetTop - (window.innerHeight / 2)," +
                    "  left: arguments[0].offsetLeft - (window.innerWidth / 2)," +
                    "  behavior: 'smooth'" +
                    "});",
                    element
                );
                Thread.sleep(500); // Allow scroll to complete
            }
        } catch (Exception e) {
            logger.warn("Failed to scroll to element", e);
            // Scrolling failure is non-fatal - interaction may still succeed
        }
    }
}