package com.mcp.selenium.handlers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ScriptHandlerTest {
    private WebDriver driver;
    private ScriptHandler scriptHandler;
    
    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        scriptHandler = new ScriptHandler(driver);
        
        // Navigate to a test page
        driver.get("https://www.example.com");
    }
    
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    public void testExecuteScript() {
        Object result = scriptHandler.executeScript("return 2 + 2");
        assertEquals("4", result);
    }
    
    @Test
    public void testExecuteScriptWithDOMManipulation() {
        Object result = scriptHandler.executeScript("document.title = 'Test Title'; return document.title;");
        assertEquals("Test Title", result);
    }
    
    @Test
    public void testGetPageSource() {
        String pageSource = scriptHandler.getPageSource();
        assertNotNull(pageSource);
        assertTrue(pageSource.contains("<html"));
        assertTrue(pageSource.contains("</html>"));
    }
    
    @Test
    public void testGetPageInfo() {
        Map<String, Object> pageInfo = scriptHandler.getPageInfo();
        assertNotNull(pageInfo);
        assertTrue(pageInfo.containsKey("url"));
        assertTrue(pageInfo.containsKey("title"));
        assertTrue(pageInfo.containsKey("readyState"));
        assertEquals("complete", pageInfo.get("readyState"));
    }
    
    @Test
    public void testEvaluateXPath() {
        Object result = scriptHandler.evaluateXPath("//h1");
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> xpathResult = (Map<String, Object>) result;
        
        // Verify the structure of the result
        assertTrue(xpathResult.containsKey("xpath"));
        assertTrue(xpathResult.containsKey("count"));
        assertTrue(xpathResult.containsKey("elements"));
        
        assertEquals("//h1", xpathResult.get("xpath"));
        
        // Should find at least one h1 element on example.com
        Integer count = (Integer) xpathResult.get("count");
        assertTrue("Should find at least one h1 element", count > 0);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> elements = (List<Map<String, Object>>) xpathResult.get("elements");
        assertEquals(count.intValue(), elements.size());
        
        // Verify first element has expected properties
        if (elements.size() > 0) {
            Map<String, Object> firstElement = elements.get(0);
            assertTrue(firstElement.containsKey("tagName"));
            assertTrue(firstElement.containsKey("text"));
            assertEquals("H1", firstElement.get("tagName"));
        }
    }
    
    @Test
    public void testScrollTo() {
        String result = scriptHandler.scrollTo(100, 200);
        assertEquals("Scrolled to position: 100, 200", result);
    }
    
    @Test
    public void testScrollBy() {
        String result = scriptHandler.scrollBy(50, 50);
        assertEquals("Scrolled by: 50, 50", result);
    }
    
    @Test
    public void testSetAndGetCookie() {
        // Set a cookie
        String setCookieResult = scriptHandler.setCookie("testCookie", "testValue", 7);
        assertEquals("Cookie set: testCookie", setCookieResult);
        
        // Get the cookie
        Object getCookieResult = scriptHandler.getCookie("testCookie");
        assertNotNull(getCookieResult);
        assertTrue(getCookieResult.toString().contains("testValue"));
    }
    
    @Test
    public void testGetAllCookies() {
        // Set some cookies first
        scriptHandler.setCookie("cookie1", "value1", 7);
        scriptHandler.setCookie("cookie2", "value2", 7);
        
        // Get all cookies
        Object result = scriptHandler.getCookie(null);
        assertNotNull(result);
        assertTrue(result instanceof String);
        
        String cookieString = (String) result;
        
        // Verify that our cookies are present
        assertTrue("Cookie1 should be present", cookieString.contains("cookie1=value1"));
        assertTrue("Cookie2 should be present", cookieString.contains("cookie2=value2"));
    }
    
    @Test
    public void testSetAndGetLocalStorage() {
        // Set local storage
        String setResult = scriptHandler.setLocalStorage("testKey", "testValue");
        assertEquals("localStorage set: testKey", setResult);
        
        // Get local storage
        Object getResult = scriptHandler.getLocalStorage("testKey");
        assertEquals("testValue", getResult);
    }
    
    @Test
    public void testGetAllLocalStorage() {
        // Set some values first
        scriptHandler.setLocalStorage("key1", "value1");
        scriptHandler.setLocalStorage("key2", "value2");
        
        // Get all local storage
        Object result = scriptHandler.getLocalStorage(null);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        Map<String, String> storage = (Map<String, String>) result;
        assertEquals("value1", storage.get("key1"));
        assertEquals("value2", storage.get("key2"));
    }
    
    @Test
    public void testGetConsoleLog() {
        // Execute a script that logs to console
        scriptHandler.executeScript("console.log('Test log message');");
        
        Object logs = scriptHandler.getConsoleLog();
        assertNotNull(logs);
        // Note: Console logs might not be available in headless mode
    }
}