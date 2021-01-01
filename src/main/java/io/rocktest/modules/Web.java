package io.rocktest.modules;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Web extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Web.class);

    private WebDriver driver;

    private void createDriver(String browser,List<String> browserOptions) {

        switch (browser) {
            case "firefox":

                driver=new FirefoxDriver();

                break;
            default:
                throw new RuntimeException("Browser "+browser+" not supported");
        }

    }


    public Map<String, Object> get(Map<String, Object> params) {

        String browser = getStringParam(params,"browser","firefox");
        List<String> browserOptions = getArrayParam(params,"browserOptions",null);

        createDriver(browser,browserOptions);
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


    @Override
    public void cleanup() {
        if(driver != null) {
            driver.quit();
        }
    }
}