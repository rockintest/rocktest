package io.rocktest.modules;

import com.paulhammant.ngwebdriver.NgWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Web extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Web.class);

    private WebDriver driver;
    private String newWindow=null;
    private Set<String> windows;


    // Update the new window, if necessary
    private void computeNewWindow() {
        Set<String> windowsNow=driver.getWindowHandles();

        if(windows.size() != windowsNow.size()) {

            //Loop through until we find a new window handle
            for (String windowHandle : windowsNow) {
                if(!windows.contains(windowHandle)) {
                    LOG.debug("New Window handle = {}",windowHandle);
                    this.newWindow=windowHandle;
                    break;
                }
            }

            windows=windowsNow;
        } else {

            // No new window created
            newWindow = null;

        }

    }

    private void createDriver(String browser,List<String> browserOptions) {

        switch (browser) {
            case "firefox":
                driver=new FirefoxDriver();
                driver.manage().deleteAllCookies();
                windows=driver.getWindowHandles();
                break;
            default:
                throw new RuntimeException("Browser "+browser+" not supported");
        }

    }


    public Map<String, Object> get(Map<String, Object> params) {

        String browser = getStringParam(params,"browser","firefox");
        List<String> browserOptions = getArrayParam(params,"browserOptions",null);

        if(driver==null) {
            createDriver(browser,browserOptions);
        }
        String url = getStringParam(params,"url");

        driver.get(url);

        return null;
    }


    public Map<String, Object> title(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        ret.put("result",driver.getTitle());

        return ret;
    }


    public Map<String, Object> url(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        ret.put("result",driver.getCurrentUrl());

        return ret;
    }

    public Map<String, Object> window(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        ret.put("result",driver.getWindowHandle());

        return ret;
    }

    public Map<String, Object> newWindow(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        if(newWindow != null)
            ret.put("result",newWindow);

        return ret;
    }


    public Map<String, Object> switchTo(Map<String, Object> params) {

        String to=getStringParam(params,"to");

        LOG.debug("Switch to {}",to);
        driver.switchTo().window(to);

        return null;
    }


    /**
     * Gets an element by a specific criteria
     * If multiple criteria are specified, the first matching is returned
     * @param params
     * @return
     */
    public By by(Map<String, Object> params) {

        By ret=null;

        // Get by link text ?
        String criteria = getStringParam(params,"by.linktext",null);
        if(criteria!=null) {
            boolean exact = getBooleanParam(params,"exact",false);

            if(exact) {
                LOG.debug("Search by link text = {}",criteria);
                ret = By.linkText(criteria);
            } else {
                LOG.debug("Search by partial link text = {}",criteria);
                ret = By.partialLinkText(criteria);
            }

            return ret;
        }

        // Get by content ?
        criteria = getStringParam(params,"by.content",null);
        if(criteria!=null) {
            LOG.debug("Search by content = {}",criteria);
            ret = By.xpath("//*[contains(text(),'"+criteria+"')]");
            return ret;
        }

        // Get by name ?
        criteria = getStringParam(params,"by.name",null);
        if(criteria!=null) {
            LOG.debug("Search by name = {}",criteria);
            ret = By.name(criteria);
            return ret;
        }

        // Get by class ?
        criteria = getStringParam(params,"by.class",null);
        if(criteria!=null) {
            LOG.debug("Search by class = {}",criteria);
            ret = By.className(criteria);
            return ret;
        }

        // Get by cssSelector ?
        criteria = getStringParam(params,"by.css",null);
        if(criteria!=null) {
            LOG.debug("Search by css = {}",criteria);
            ret = By.cssSelector(criteria);
            return ret;
        }

        // Get by id ?
        criteria = getStringParam(params,"by.id",null);
        if(criteria!=null) {
            LOG.debug("Search by id = {}",criteria);
            ret = By.id(criteria);
            return ret;
        }

        // Get by tag ?
        criteria = getStringParam(params,"by.tag",null);
        if(criteria!=null) {
            LOG.debug("Search by tag = {}",criteria);
            ret = By.tagName(criteria);
            return ret;
        }

        // Get by xpath ?
        criteria = getStringParam(params,"by.xpath",null);
        if(criteria!=null) {
            LOG.debug("Search by xpath = {}",criteria);
            ret = By.xpath(criteria);
            return ret;
        }

        throw new RuntimeException("Unknown criteria");

    }


    public Map<String, Object> attribute(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        Integer wait = getIntParam(params,"wait",10);
        String attribute = getStringParam(params,"name");

        WebElement elt=null;
        if(wait==0) {
            elt = driver.findElement(by(params));
        } else {
            LOG.debug("Wait for element to be present");
            elt = new WebDriverWait(driver, wait)
                    .until(ExpectedConditions.presenceOfElementLocated(by(params)));

        }

        ret.put("result",elt.getAttribute(attribute));

        return ret;
    }


    public Map<String, Object> css(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        Integer wait = getIntParam(params,"wait",10);
        String attribute = getStringParam(params,"name");

        WebElement elt=null;
        if(wait==0) {
            elt = driver.findElement(by(params));
        } else {
            LOG.debug("Wait for element to be present");
            elt = new WebDriverWait(driver, wait)
                    .until(ExpectedConditions.presenceOfElementLocated(by(params)));

        }

        ret.put("result",elt.getCssValue(attribute));

        return ret;
    }



    public Map<String, Object> text(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        Integer wait = getIntParam(params,"wait",10);

        WebElement elt=null;
        if(wait==0) {
            elt = driver.findElement(by(params));
        } else {
            LOG.debug("Wait for element to be present");
            elt = new WebDriverWait(driver, wait)
                    .until(ExpectedConditions.presenceOfElementLocated(by(params)));

        }

        ret.put("result",elt.getText());

        return ret;
    }



    public Map<String, Object> sendKeys(Map<String, Object> params) {

        Integer wait = getIntParam(params,"wait",10);
        String value = getStringParam(params,"value");

        WebElement elt=null;
        if(wait==0) {
            elt = driver.findElement(by(params));
        } else {
            LOG.debug("Wait for element to be present");
            elt = new WebDriverWait(driver, wait)
                    .until(ExpectedConditions.presenceOfElementLocated(by(params)));

        }

        elt.sendKeys(value);

        return null;
    }


    public Map<String, Object> clear(Map<String, Object> params) {

        Integer wait = getIntParam(params,"wait",10);

        LOG.debug("Wait for element to be present");
        WebElement elt = new WebDriverWait(driver, wait)
                .until(ExpectedConditions.presenceOfElementLocated(by(params)));
        elt.clear();

        return null;
    }

    public Map<String, Object> click(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        Integer wait = getIntParam(params,"wait",10);
        Boolean sw = getBooleanParam(params,"switch",true);

        LOG.debug("Wait for element to be clickable");
        WebElement elt = new WebDriverWait(driver, wait)
                .until(ExpectedConditions.elementToBeClickable(by(params)));
        elt.click();

        computeNewWindow();

        if(newWindow != null && sw)
            driver.switchTo().window(newWindow);

        return ret;
    }


    public Map<String, Object> quit(Map<String, Object> params) {

        if(driver!=null) {
            driver.quit();
            driver=null;
        }
        return null;
    }


    @Override
    public void cleanup() {
        if(driver != null) {

            LOG.info ("Cleaning Web browser");
            driver.quit();
        }
    }
}