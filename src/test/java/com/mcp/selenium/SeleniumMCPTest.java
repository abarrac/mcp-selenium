package com.mcp.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.Assert.*;

public class SeleniumMCPTest {
    private SeleniumMCP seleniumMCP;
    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        seleniumMCP = new SeleniumMCP();
        objectMapper = new ObjectMapper();
    }
    
    @After
    public void tearDown() {
        seleniumMCP.cleanup();
    }
    
    @Test
    public void testStartBrowser() {
        ObjectNode args = objectMapper.createObjectNode();
        args.put("browser", "chrome");
        
        Object result = seleniumMCP.executeTool("start_browser", args);
        assertEquals("Browser started successfully", result);
    }
    
    @Test
    public void testNavigate() {
        ObjectNode args = objectMapper.createObjectNode();
        args.put("url", "https://www.example.com");
        
        Object result = seleniumMCP.executeTool("navigate", args);
        assertNotNull(result);
        assertTrue(result.toString().contains("Navigated to"));
    }
    
    @Test
    public void testCompleteWorkflow() {
        // Start browser
        ObjectNode startArgs = objectMapper.createObjectNode();
        startArgs.put("browser", "chrome");
        seleniumMCP.executeTool("start_browser", startArgs);
        
        // Navigate
        ObjectNode navArgs = objectMapper.createObjectNode();
        navArgs.put("url", "https://formy-project.herokuapp.com/form");
        Object navResult = seleniumMCP.executeTool("navigate", navArgs);
        assertTrue(navResult.toString().contains("Navigated to"));
        
        // Fill a field
        ObjectNode fillArgs = objectMapper.createObjectNode();
        fillArgs.put("by", "id");
        fillArgs.put("value", "first-name");
        fillArgs.put("text", "John");
        Object fillResult = seleniumMCP.executeTool("send_keys", fillArgs);
        assertTrue(fillResult.toString().contains("Filled element"));
        
        // Take screenshot
        ObjectNode screenshotArgs = objectMapper.createObjectNode();
        Object screenshotResult = seleniumMCP.executeTool("take_screenshot", screenshotArgs);
        assertNotNull(screenshotResult);
        assertTrue(screenshotResult instanceof Map);
        
        // Get page info
        ObjectNode infoArgs = objectMapper.createObjectNode();
        Object infoResult = seleniumMCP.executeTool("getPageInfo", infoArgs);
        assertNotNull(infoResult);
        assertTrue(infoResult instanceof Map);
        
        // Close session
        ObjectNode closeArgs = objectMapper.createObjectNode();
        Object closeResult = seleniumMCP.executeTool("close_session", closeArgs);
        assertEquals("Browser session closed", closeResult);
    }
    
    @Test
    public void testInvalidTool() {
        // Temporarily disable logging for this test
        Logger seleniumLogger = (Logger) LoggerFactory.getLogger(SeleniumMCP.class);
        Level originalLevel = seleniumLogger.getLevel();
        
        try {
            // Disable ERROR logging temporarily
            seleniumLogger.setLevel(Level.OFF);
            
            ObjectNode args = objectMapper.createObjectNode();
            Object result = seleniumMCP.executeTool("invalid_tool", args);
            
            assertTrue(result instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> errorMap = (Map<String, Object>) result;
            assertTrue(errorMap.containsKey("error"));
            assertTrue(errorMap.get("error").toString().contains("Unknown tool"));
        } finally {
            // Restore original log level
            seleniumLogger.setLevel(originalLevel);
        }
    }
    
    @Test
    public void testNoActiveSession() {
        ObjectNode args = objectMapper.createObjectNode();
        args.put("by", "id");
        args.put("value", "test");
        
        Object result = seleniumMCP.executeTool("click_element", args);
        assertEquals("No browser session active", result);
    }
    
    @Test
    public void testXPathSelector() {
        // Start browser and navigate
        ObjectNode startArgs = objectMapper.createObjectNode();
        startArgs.put("browser", "chrome");
        seleniumMCP.executeTool("start_browser", startArgs);
        
        ObjectNode navArgs = objectMapper.createObjectNode();
        navArgs.put("url", "https://www.example.com");
        seleniumMCP.executeTool("navigate", navArgs);
        
        // Test XPath selector
        ObjectNode xpathArgs = objectMapper.createObjectNode();
        xpathArgs.put("by", "xpath");
        xpathArgs.put("value", "//h1");
        Object result = seleniumMCP.executeTool("get_element_text", xpathArgs);
        assertNotNull(result);
        assertTrue(result.toString().length() > 0);
    }
}