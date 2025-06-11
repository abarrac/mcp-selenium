package com.mcp.selenium.handlers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.Map;

import static org.junit.Assert.*;

public class ElementHandlerTest {
    private WebDriver driver;
    private ElementHandler elementHandler;
    
    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        elementHandler = new ElementHandler(driver);
        
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
    public void testFindAndFillElement() {
        // Test filling a text field
        String result = elementHandler.fill("id=first-name", "John", true);
        assertTrue(result.contains("Filled element"));
        assertTrue(result.contains("John"));
    }
    
    @Test
    public void testClickElement() {
        // Test clicking a radio button
        String result = elementHandler.click("id=radio-button-1");
        assertTrue(result.contains("Clicked element"));
    }
    
    @Test
    public void testGetText() {
        // Navigate to a page with text
        driver.get("https://www.example.com");
        String text = elementHandler.getText("tag=h1");
        assertNotNull(text);
        assertTrue(text.length() > 0);
    }
    
    @Test
    public void testIsVisible() {
        boolean isVisible = elementHandler.isVisible("id=first-name");
        assertTrue(isVisible);
        
        // Test non-existent element
        boolean notVisible = elementHandler.isVisible("id=non-existent-element");
        assertFalse(notVisible);
    }
    
    @Test
    public void testIsEnabled() {
        boolean isEnabled = elementHandler.isEnabled("id=first-name");
        assertTrue(isEnabled);
    }
    
    @Test
    public void testSelectDropdown() {
        String result = elementHandler.select("id=select-menu", "0-1", "text");
        assertTrue(result.contains("Selected"));
    }
    
    @Test
    public void testHover() {
        String result = elementHandler.hover("id=first-name");
        assertTrue(result.contains("Hovered over element"));
    }
    
    @Test
    public void testDoubleClick() {
        String result = elementHandler.doubleClick("id=first-name");
        assertTrue(result.contains("Double-clicked element"));
    }
    
    @Test
    public void testRightClick() {
        String result = elementHandler.rightClick("id=first-name");
        assertTrue(result.contains("Right-clicked element"));
    }
    
    @Test
    public void testWaitForElement() {
        String result = elementHandler.waitForElement("id=first-name", 5);
        assertTrue(result.contains("Element found"));
    }
    
    @Test
    public void testFindElements() {
        Object result = elementHandler.findElements("tag=input");
        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, String>> elements = (java.util.List<Map<String, String>>) result;
        assertTrue(elements.size() > 0);
    }
    
    @Test
    public void testGetAttribute() {
        Map<String, String> result = elementHandler.getAttribute("id=first-name", "placeholder");
        assertNotNull(result);
        assertTrue(result.containsKey("attribute"));
        assertTrue(result.containsKey("value"));
        assertEquals("placeholder", result.get("attribute"));
        assertEquals("Enter first name", result.get("value"));
    }
}
