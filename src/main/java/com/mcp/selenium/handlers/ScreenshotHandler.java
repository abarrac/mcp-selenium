package com.mcp.selenium.handlers;

import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * ScreenshotHandler - Advanced visual capture and documentation system.
 * 
 * Provides comprehensive screenshot capabilities for web automation testing,
 * documentation, and visual verification. Supports multiple capture modes
 * including viewport, element-specific, and full-page screenshots.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>High-quality PNG screenshot capture</li>
 *   <li>Base64 encoding for easy transmission</li>
 *   <li>Element-specific screenshot isolation</li>
 *   <li>Full-page capture with tiling algorithm</li>
 *   <li>Automatic element positioning and scrolling</li>
 *   <li>Rich metadata including timestamps and page context</li>
 * </ul>
 * 
 * <p>Output Formats:
 * All screenshots are captured in PNG format and can be returned as:
 * <ul>
 *   <li>Base64-encoded strings for network transmission</li>
 *   <li>Saved files with automatic naming</li>
 *   <li>Rich metadata maps with capture context</li>
 * </ul>
 * 
 * @author Alberto Barragán
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScreenshotHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotHandler.class);
    
    /** WebDriver instance for screenshot operations */
    private final WebDriver driver;

    /**
     * Constructs a new ScreenshotHandler with the specified WebDriver.
     * 
     * Initializes the handler to work with the provided WebDriver instance
     * for all screenshot capture operations.
     * 
     * @param driver the WebDriver instance to use for screenshot capture
     */
    public ScreenshotHandler(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Captures a screenshot of the current browser viewport.
     * 
     * Takes a high-quality PNG screenshot of the visible browser area and
     * returns comprehensive metadata including Base64-encoded image data,
     * timestamps, and page context information.
     * 
     * @return detailed screenshot information map containing:
     *         - type: "screenshot"
     *         - format: "png"
     *         - encoding: "base64"
     *         - data: Base64-encoded image data
     *         - timestamp: capture time in milliseconds
     *         - url: current page URL
     *         - title: current page title
     * @throws RuntimeException if screenshot capture fails
     */
    public Map<String, Object> takeScreenshot() {
        try {
            TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
            byte[] screenshotBytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);

            String base64 = Base64.encodeBase64String(screenshotBytes);

            Map<String, Object> result = new HashMap<>();
            result.put("type", "screenshot");
            result.put("format", "png");
            result.put("encoding", "base64");
            result.put("data", base64);
            result.put("timestamp", System.currentTimeMillis());
            result.put("url", driver.getCurrentUrl());
            result.put("title", driver.getTitle());

            return result;
        } catch (Exception e) {
            logger.error("Failed to take screenshot", e);
            throw new RuntimeException("Failed to take screenshot: " + e.getMessage());
        }
    }

    /**
     * Captures a screenshot of a specific element on the page.
     * 
     * Locates the specified element, scrolls it into optimal view, and
     * captures an isolated screenshot containing only that element.
     * Perfect for focused testing and element-specific documentation.
     * 
     * @param selector the element selector to capture
     * @return detailed element screenshot information map containing:
     *         - type: "element_screenshot"
     *         - selector: the selector used to find the element
     *         - format: "png"
     *         - encoding: "base64"
     *         - data: Base64-encoded image data
     *         - timestamp: capture time in milliseconds
     * @throws RuntimeException if element cannot be found or captured
     */
    public Map<String, Object> takeElementScreenshot(String selector) {
        try {
            By by = getBy(selector);
            WebElement element = driver.findElement(by);

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});",
                    element
            );
            Thread.sleep(500); // Allow scroll to complete

            byte[] screenshotBytes = element.getScreenshotAs(OutputType.BYTES);
            String base64 = Base64.encodeBase64String(screenshotBytes);

            Map<String, Object> result = new HashMap<>();
            result.put("type", "element_screenshot");
            result.put("selector", selector);
            result.put("format", "png");
            result.put("encoding", "base64");
            result.put("data", base64);
            result.put("timestamp", System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            logger.error("Failed to take element screenshot", e);
            throw new RuntimeException("Failed to take element screenshot: " + e.getMessage());
        }
    }

    /**
     * Saves a screenshot to the local filesystem with automatic naming.
     * 
     * Captures a viewport screenshot and saves it as a PNG file. If no
     * filename is provided, generates a timestamp-based name automatically.
     * 
     * @param filename the desired filename (optional - auto-generated if null/empty)
     * @return success message with the absolute file path
     * @throws RuntimeException if screenshot capture or file save fails
     */
    public String saveScreenshot(String filename) {
        try {
            TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
            File srcFile = screenshotDriver.getScreenshotAs(OutputType.FILE);

            if (filename == null || filename.isEmpty()) {
                filename = "screenshot_" + System.currentTimeMillis() + ".png";
            }

            if (!filename.endsWith(".png")) {
                filename += ".png";
            }

            File destFile = new File(filename);
            srcFile.renameTo(destFile);

            return "Screenshot saved to: " + destFile.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save screenshot: " + e.getMessage());
        }
    }

    /**
     * Captures a full-page screenshot using advanced tiling algorithm.
     * 
     * Creates a complete screenshot of the entire page content, including
     * areas below the fold that require scrolling. Uses intelligent tiling
     * to capture the full page in sections and seamlessly combines them
     * into a single high-resolution image.
     * 
     * <p>Algorithm:
     * <ol>
     *   <li>Calculate page dimensions and required tiles</li>
     *   <li>Capture viewport-sized tiles systematically</li>
     *   <li>Composite tiles into full-page image</li>
     *   <li>Restore original scroll position</li>
     * </ol>
     * 
     * @return detailed full-page screenshot information map containing:
     *         - type: "full_page_screenshot"
     *         - format: "png"
     *         - encoding: "base64"
     *         - data: Base64-encoded full-page image
     *         - width: total page width in pixels
     *         - height: total page height in pixels
     *         - timestamp: capture time in milliseconds
     * @throws RuntimeException if full-page capture fails
     */
    public Map<String, Object> takeFullPageScreenshot() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Calculate complete page dimensions
            Long scrollHeight = (Long) js.executeScript("return document.body.scrollHeight");
            Long scrollWidth = (Long) js.executeScript("return document.body.scrollWidth");
            Long viewportHeight = (Long) js.executeScript("return window.innerHeight");
            Long viewportWidth = (Long) js.executeScript("return window.innerWidth");

            // Calculate tiling requirements
            int tilesX = (int) Math.ceil(scrollWidth.doubleValue() / viewportWidth.doubleValue());
            int tilesY = (int) Math.ceil(scrollHeight.doubleValue() / viewportHeight.doubleValue());

            // Create composite image canvas
            BufferedImage fullImage = new BufferedImage(
                    scrollWidth.intValue(),
                    scrollHeight.intValue(),
                    BufferedImage.TYPE_INT_RGB
            );

            // Systematic tile capture and composition
            for (int y = 0; y < tilesY; y++) {
                for (int x = 0; x < tilesX; x++) {
                    // Position viewport for tile capture
                    js.executeScript(String.format(
                            "window.scrollTo(%d, %d);",
                            x * viewportWidth,
                            y * viewportHeight
                    ));
                    Thread.sleep(200); // Allow scroll to stabilize

                    // Capture current viewport tile
                    byte[] tileBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    BufferedImage tile = ImageIO.read(new ByteArrayInputStream(tileBytes));

                    // Calculate composite position
                    int posX = x * viewportWidth.intValue();
                    int posY = y * viewportHeight.intValue();

                    // Add tile to composite image
                    fullImage.getGraphics().drawImage(tile, posX, posY, null);
                }
            }

            // Convert composite to Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(fullImage, "png", baos);
            String base64 = Base64.encodeBase64String(baos.toByteArray());

            // Restore original scroll position
            js.executeScript("window.scrollTo(0, 0);");

            Map<String, Object> result = new HashMap<>();
            result.put("type", "full_page_screenshot");
            result.put("format", "png");
            result.put("encoding", "base64");
            result.put("data", base64);
            result.put("width", scrollWidth);
            result.put("height", scrollHeight);
            result.put("timestamp", System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            logger.error("Failed to take full page screenshot", e);
            throw new RuntimeException("Failed to take full page screenshot: " + e.getMessage());
        }
    }

    /**
     * Converts selector strings to appropriate By locator objects.
     * 
     * Internal utility method that supports all major selector types with
     * automatic detection based on string patterns. Provides consistent
     * element location across all screenshot operations.
     * 
     * @param selector the selector string to convert
     * @return appropriate By locator object
     * @see ElementHandler#getBy(String) for detailed selector format documentation
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
            return By.cssSelector(selector); // Default to CSS selector
        }
    }
}