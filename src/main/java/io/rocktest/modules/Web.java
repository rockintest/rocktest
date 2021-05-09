package io.rocktest.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocktest.RockException;
import io.rocktest.RocktestApplication;
import io.rocktest.modules.annotations.RockWord;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.internal.WebElementToJsonConverter;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Web.class);

    private WebDriver driver;
    private String newWindow=null;
    private Set<String> windows;
    private Map<String,List<WebElement>> elements = new HashMap<>();
    private Actions actions=null;

    // Update the new window, if necessary
    private void computeNewWindow() {
        Set<String> windowsNow=driver.getWindowHandles();

        if(LOG.isDebugEnabled()) {
            for (String windowHandle : windowsNow) {
                LOG.debug("Existing window: {}", windowHandle);
            }
        }


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
            //newWindow = null;

        }

    }

    private void createDriver(String browser,List<String> browserOptions) {

        switch (browser) {
            case "firefox":
                FirefoxOptions ffoptions = new FirefoxOptions();

                if(browserOptions != null) {
                    ffoptions.addArguments(browserOptions);
                }

                driver=new FirefoxDriver(ffoptions);
                break;
            case "chrome":
                ChromeOptions options = new ChromeOptions();

                if(browserOptions != null) {
                    options.addArguments(browserOptions);
                }

                driver=new ChromeDriver(options);
                break;
            default:
                fail("Browser "+browser+" not supported");
        }

        driver.manage().deleteAllCookies();
        actions = new Actions(driver);
        windows=driver.getWindowHandles();

    }


    @RockWord(keyword="web.hide")
    public Map<String, Object> hide(Map<String, Object> params) {

        driver.manage().window().maximize();
        driver.manage().window().setPosition(new Point(0, -2000));
        return null;
    }

    @RockWord(keyword="web.show")
    public Map<String, Object> show(Map<String, Object> params) {

        driver.manage().window().maximize();
        driver.manage().window().setPosition(new Point(0, 0));
        return null;
    }


    @RockWord(keyword="web.get")
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


    @RockWord(keyword="web.title")
    public Map<String, Object> title(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        ret.put("result",driver.getTitle());

        return ret;
    }


    @RockWord(keyword="web.url")
    public Map<String, Object> url(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        ret.put("result",driver.getCurrentUrl());

        return ret;
    }

    @RockWord(keyword="web.window")
    public Map<String, Object> window(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        ret.put("result",driver.getWindowHandle());

        return ret;
    }

    @RockWord(keyword="web.newwindow")
    public Map<String, Object> newWindow(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        computeNewWindow();

        if(newWindow != null)
            ret.put("result",newWindow);

        return ret;
    }

    @RockWord(keyword="web.switch")
    public Map<String, Object> switchTo(Map<String, Object> params) {

        String to=getStringParam(params,"window",null);
        if(to != null) {
            LOG.debug("Switch to window {}",to);
            driver.switchTo().window(to);
            return null;
        }

        to=getStringParam(params,"default",null);
        if(to != null) {
            LOG.debug("Switch to default content");
            driver.switchTo().defaultContent();
            return null;
        }

        Map<String, Object> frameParams=(Map<String, Object>) params.get("frame");
        if(frameParams != null) {

            String id=getStringParam(frameParams,"id",null);
            if(id!=null) {
                driver.switchTo().frame(id);
                return null;
            }

            String name=getStringParam(frameParams,"name",null);
            if(name!=null) {
                driver.switchTo().frame(name);
                return null;
            }

            Integer index=getIntParam(frameParams,"index",null);
            if(index!=null) {
                driver.switchTo().frame(index);
                return null;
            }

            WebElement elt=getElement(frameParams);
            driver.switchTo().frame(elt);
            return null;

        }

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

        fail("Unknown criteria");
        return null;
    }


    @RockWord(keyword="web.attribute")
    public Map<String, Object> attribute(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();
        String attribute = getStringParam(params,"name");

        WebElement elt=getElement(params);
        ret.put("result",elt.getAttribute(attribute));

        return ret;
    }

    @RockWord(keyword="web.css")
    public Map<String, Object> css(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();
        String attribute = getStringParam(params,"name");

        WebElement elt=getElement(params);
        ret.put("result",elt.getCssValue(attribute));

        return ret;
    }

    @RockWord(keyword="web.tag")
    public Map<String, Object> tag(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        WebElement elt=getElement(params);
        ret.put("result",elt.getTagName());

        return ret;

    }

    @RockWord(keyword="web.text")
    public Map<String, Object> text(Map<String, Object> params) {

        Map<String,Object> ret = new HashMap<>();

        WebElement elt=getElement(params);
        ret.put("result",elt.getText());

        return ret;

    }


    @RockWord(keyword="web.search.text")
    public Map<String, Object> searchText(Map<String, Object> params) {

        Map<String,Object> ret = new HashMap<>();

        String list=getStringParam(params,"list","default");
        Integer pos=getIntParam(params,"order",null);

        List<WebElement> l=elements.get(list);

        if(l == null) {
            LOG.info("No list {} found",list);
            return null;
        }

        if(pos != null) {
            WebElement elt = l.get(pos);
            ret.put("result",elt.getText());
        } else {

            StringBuilder sb=new StringBuilder();

            sb.append("[");

            for (WebElement elt:l) {
                sb.append("\"");
                sb.append(elt.getText());
                sb.append("\",");
            }

            // Remove extra ","
            sb.setLength(sb.length() - 1);
            sb.append("]");

            ret.put("result",sb.toString());

        }

        return ret;

    }




    @RockWord(keyword="web.search")
    public Map<String, Object> search(Map<String, Object> params) {

        Map<String,Object> ret = new HashMap<>();

        String list=getStringParam(params,"list","default");

        try {

            List<WebElement> elts = getElements(params);
            elements.put(list,elts);
            LOG.info("{} elements stored in list {}",elts.size(),list);

        } catch(RockException e) {
            LOG.info("No elements found for {}",params.toString());
        }

        return ret;

    }


    @RockWord(keyword="web.submit")
    public Map<String, Object> submit(Map<String, Object> params) {

        WebElement elt = getElement(params);
        elt.submit();
        return null;
    }

    @RockWord(keyword="web.sendkeys")
    public Map<String, Object> sendKeys(Map<String, Object> params) {

        WebElement elt = getElement(params);
        String value = getStringParam(params,"value");
        elt.sendKeys(value);

        return null;
    }


    private WebElement getElement(Map<String, Object> params) {

        Map<String, Object> paramsFrom=(Map<String, Object>)params.get("from");

        if(paramsFrom != null) {

            WebElement from = getElement(paramsFrom);
            Integer order = getIntParam(params, "order", 1);
            By by = by(params);
            return from.findElements(by).get(order -1);

        } else {

            Integer order = getIntParam(params, "order", 1);
            By by = by(params);
            wait(params, by);
            return driver.findElements(by).get(order - 1);

        }
    }


    private List<WebElement> getElements(Map<String, Object> params) {

        Map<String, Object> paramsFrom=(Map<String, Object>)params.get("from");

        if(paramsFrom != null) {

            WebElement from = getElement(paramsFrom);
            By by = by(params);
            return from.findElements(by);

        } else {

            By by = by(params);
            wait(params, by);
            return driver.findElements(by);

        }
    }


    @RockWord(keyword="web.clear")
    public Map<String, Object> clear(Map<String, Object> params) {

        getElement(params).clear();
        return null;

    }


    private void wait(Map<String, Object> params, By by) {
        Integer wait = getIntParam(params,"wait",10);
        if(wait > 0) {
            LOG.debug("Wait for element to be present");

            try {
                new WebDriverWait(driver, wait)
                        .until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
            } catch(TimeoutException e) {
                throw new RockException("Element not present: "+by.toString(),e);
            }
        }
    }

    @RockWord(keyword="web.move")
    public Map<String, Object> move(Map<String, Object> params) {

        int x=getIntParam(params,"xOffset",0);
        int y=getIntParam(params,"yOffset",0);

        WebElement elt = getElement(params);

        actions.moveToElement(elt,x,y).perform();

        return null;
    }

    @RockWord(keyword="web.count")
    public Map<String, Object> count(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        By by=by(params);

        try {
            wait(params, by);
        } catch(RockException e) {
            if(e.getCause() != null && e.getCause() instanceof TimeoutException) {
                ret.put("result",0);
                return ret;
            } else {
                throw e;
            }
        }

        List<WebElement> l = getElements(params); //driver.findElements(by);

        ret.put("result",l.size());

        return ret;
    }

    @RockWord(keyword="web.click")
    public Map<String, Object> click(Map<String, Object> params) {

        Map<String,Object> ret=new HashMap<>();

        Integer wait = getIntParam(params,"wait",10);
        Boolean sw = getBooleanParam(params,"switch",true);

        WebElement elt = getElement(params);

        LOG.debug("Wait for element to be clickable");
        new WebDriverWait(driver, wait)
                .until(ExpectedConditions.elementToBeClickable(elt));
        elt.click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        computeNewWindow();

        if(newWindow != null && sw) {
            LOG.debug("Switching to new window {}",newWindow);
            driver.switchTo().window(newWindow);
        }

        return ret;
    }

    @RockWord(keyword="web.quit")
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