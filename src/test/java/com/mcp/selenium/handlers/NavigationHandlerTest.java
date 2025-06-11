package com.mcp.selenium.handlers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import static org.junit.Assert.*;

public class NavigationHandlerTest {
    private WebDriver driver;
    private NavigationHandler navigationHandler;
    
    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        navigationHandler = new NavigationHandler(driver);
    }
    
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    public void testNavigate() {
        String result = navigationHandler.navigate("https://www.google.com");
        assertTrue(result.contains("Navigated to: https://www.google.com"));
        assertTrue(result.contains("Title:"));
    }
    
    @Test
    public void testGetCurrentUrl() {
        navigationHandler.navigate("https://www.google.com");
        String url = navigationHandler.getCurrentUrl();
        assertTrue(url.contains("google.com"));
    }
    
    @Test
    public void testGetTitle() {
        navigationHandler.navigate("https://www.google.com");
        String title = navigationHandler.getTitle();
        assertNotNull(title);
        assertTrue(title.length() > 0);
    }
    
    @Test
    public void testRefresh() {
        navigationHandler.navigate("https://www.google.com");
        String result = navigationHandler.refresh();
        assertEquals("Page refreshed. Current URL: https://www.google.com/", result);
    }
    
    @Test
    public void testBackAndForward() {
        // Navigate to first page
        navigationHandler.navigate("https://www.google.com");
        String firstUrl = driver.getCurrentUrl();
        
        // Navigate to second page
        navigationHandler.navigate("https://www.example.com");
        String secondUrl = driver.getCurrentUrl();
        
        // Go back
        navigationHandler.goBack();
        assertEquals(firstUrl, driver.getCurrentUrl());
        
        // Go forward
        navigationHandler.goForward();
        assertEquals(secondUrl, driver.getCurrentUrl());
    }
}
