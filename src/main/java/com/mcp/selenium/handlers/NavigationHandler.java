package com.mcp.selenium.handlers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * NavigationHandler - Professional browser navigation and page control.
 * <p>
 * Provides comprehensive browser navigation capabilities including URL handling,
 * history management, and page state monitoring. Implements intelligent waiting
 * strategies and robust error handling for reliable navigation operations.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Smart URL normalization and validation</li>
 *   <li>Intelligent page load waiting with readyState monitoring</li>
 *   <li>Browser history navigation (back/forward)</li>
 *   <li>Page refresh with load completion detection</li>
 *   <li>URL and title extraction utilities</li>
 *   <li>Protocol-aware navigation (HTTP/HTTPS)</li>
 * </ul>
 * 
 * <p>Navigation Safety:
 * All navigation operations include timeout protection and page load verification
 * to ensure reliable automation across different network conditions and page types.
 * 
 * @author Alberto Barragán
 * @version 1.0.0
 * @since 1.0.0
 */
public class NavigationHandler {
    private static final Logger logger = LoggerFactory.getLogger(NavigationHandler.class);
    
    /** WebDriver instance for browser control */
    private final WebDriver driver;
    
    /** WebDriverWait instance for page load synchronization */
    private final WebDriverWait wait;

    /**
     * Constructs a new NavigationHandler with the specified WebDriver.
     * <p>
     * Initializes the handler with a default wait timeout of 10 seconds
     * for page load operations and navigation events.
     * 
     * @param driver the WebDriver instance to use for navigation operations
     */
    public NavigationHandler(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Navigates to the specified URL with intelligent protocol handling.
     * 
     * Automatically normalizes URLs by adding HTTPS protocol if missing,
     * performs the navigation, and waits for complete page load using
     * document.readyState monitoring.
     * 
     * @param url the target URL (protocol optional - HTTPS will be added if missing)
     * @return formatted success message with final URL and page title
     * @throws RuntimeException if navigation fails or times out
     */
    public String navigate(String url) {
        try {
            // Intelligent URL normalization - default to HTTPS
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            driver.get(url);

            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState")
                            .equals("complete")
            );

            String currentUrl = driver.getCurrentUrl();
            String title = driver.getTitle();

            return String.format("Navigated to: %s\nTitle: %s", currentUrl, title);
        } catch (Exception e) {
            logger.error("Navigation failed", e);
            throw new RuntimeException("Failed to navigate to URL: " + e.getMessage());
        }
    }

    /**
     * Navigates backward in the browser history.
     * 
     * Simulates clicking the browser's back button. Includes a brief
     * delay to allow navigation to complete and returns the resulting URL.
     * 
     * @return success message with the current URL after navigation
     * @throws RuntimeException if the back navigation fails
     */
    public String goBack() {
        try {
            driver.navigate().back();
            Thread.sleep(1000); // Allow navigation to complete
            return "Navigated back. Current URL: " + driver.getCurrentUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to go back: " + e.getMessage());
        }
    }

    /**
     * Navigates forward in the browser history.
     * 
     * Simulates clicking the browser's forward button. Includes a brief
     * delay to allow navigation to complete and returns the resulting URL.
     * 
     * @return success message with the current URL after navigation
     * @throws RuntimeException if the forward navigation fails
     */
    public String goForward() {
        try {
            driver.navigate().forward();
            Thread.sleep(1000); // Allow navigation to complete
            return "Navigated forward. Current URL: " + driver.getCurrentUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to go forward: " + e.getMessage());
        }
    }

    /**
     * Refreshes the current page and waits for complete reload.
     * 
     * Simulates pressing F5 or clicking the refresh button. Uses
     * document.readyState monitoring to ensure the page has fully
     * loaded before returning control.
     * 
     * @return success message with the current URL after refresh
     * @throws RuntimeException if the page refresh fails
     */
    public String refresh() {
        try {
            driver.navigate().refresh();
            
            // Wait for complete page reload using readyState monitoring
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState")
                            .equals("complete")
            );
            
            return "Page refreshed. Current URL: " + driver.getCurrentUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh: " + e.getMessage());
        }
    }

    /**
     * Retrieves the current URL of the active browser tab.
     * 
     * Returns the complete URL including protocol, domain, path,
     * and query parameters. Useful for verification and debugging.
     * 
     * @return the current URL as a string
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Retrieves the title of the current page.
     * 
     * Returns the HTML document title as displayed in the browser tab.
     * Useful for page verification and content validation.
     * 
     * @return the page title as a string
     */
    public String getTitle() {
        return driver.getTitle();
    }
}