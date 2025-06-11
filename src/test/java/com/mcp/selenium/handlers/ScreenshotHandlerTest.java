package com.mcp.selenium.handlers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.Assert.*;

public class ScreenshotHandlerTest {
    private WebDriver driver;
    private ScreenshotHandler screenshotHandler;
    
    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        screenshotHandler = new ScreenshotHandler(driver);
        
        // Navigate to a test page
        driver.get("https://formy-project.herokuapp.com/form");
    }
    
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    public void testTakeScreenshot() {
        Map<String, Object> result = screenshotHandler.takeScreenshot();
        assertNotNull(result);
        assertTrue(result.containsKey("type"));
        assertTrue(result.containsKey("data"));
        assertEquals("screenshot", result.get("type"));
        
        String base64Data = (String) result.get("data");
        assertNotNull(base64Data);
        assertTrue(base64Data.length() > 100); // Should be a substantial base64 string
    }
    
    @Test
    public void testTakeElementScreenshot() {
        Map<String, Object> result = screenshotHandler.takeElementScreenshot("id=first-name");
        assertNotNull(result);
        assertTrue(result.containsKey("type"));
        assertTrue(result.containsKey("data"));
        assertEquals("element_screenshot", result.get("type"));
        
        String base64Data = (String) result.get("data");
        assertNotNull(base64Data);
        assertTrue(base64Data.length() > 100);
    }
    
    @Test
    public void testTakeFullPageScreenshot() {
        Map<String, Object> result = screenshotHandler.takeFullPageScreenshot();
        assertNotNull(result);
        assertTrue(result.containsKey("type"));
        assertTrue(result.containsKey("data"));
        assertEquals("full_page_screenshot", result.get("type"));
        
        String base64Data = (String) result.get("data");
        assertNotNull(base64Data);
        assertTrue(base64Data.length() > 100);
    }
    
    @Test
    public void testScreenshotNonExistentElement() {
        // Temporarily disable logging for this test
        Logger screenshotLogger = (Logger) LoggerFactory.getLogger(ScreenshotHandler.class);
        Logger elementLogger = (Logger) LoggerFactory.getLogger(ElementHandler.class);
        Level originalScreenshotLevel = screenshotLogger.getLevel();
        Level originalElementLevel = elementLogger.getLevel();
        
        try {
            // Disable ERROR logging temporarily
            screenshotLogger.setLevel(Level.OFF);
            elementLogger.setLevel(Level.OFF);
            
            screenshotHandler.takeElementScreenshot("id=non-existent");
            fail("Should have thrown an exception");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to take element screenshot"));
        } finally {
            // Restore original log levels
            screenshotLogger.setLevel(originalScreenshotLevel);
            elementLogger.setLevel(originalElementLevel);
        }
    }
}