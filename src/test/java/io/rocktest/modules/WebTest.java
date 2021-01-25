package io.rocktest.modules;

import java.net.MalformedURLException;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;


public class WebTest {
    @Test
    public void testGoogleSearch() throws InterruptedException, MalformedURLException {
        // Optional. If not specified, WebDriver searches the PATH for chromedriver.
        //System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");

/*        WebDriver driver = new RemoteWebDriver(
                new URL("http://127.0.0.1:9515"),
                new ChromeOptions());*/

        WebDriver driver = new FirefoxDriver();
        driver.get("http://www.google.com/");
        Thread.sleep(5000);  // Let the user actually see something!
        WebElement searchBox = driver.findElement(By.name("q"));
        searchBox.sendKeys("ChromeDriver");
        searchBox.submit();
        Thread.sleep(5000);  // Let the user actually see something!
        driver.quit();
    }
}
