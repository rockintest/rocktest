package io.rocktest.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.rocktest.RockTest;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class DateTest extends RockTest {


    @Test
    public void now() throws JsonProcessingException {

        Date module=new Date();
        module.setScenario(scenario);

        HashMap params=new HashMap();
        Map ret=module.now(params);

        String pattern = "dd/MM/yyyy";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern(pattern);
        String formattedDate = now.format(myFormatObj);

        String item=(String)ret.get("result");
        Assert.assertEquals(formattedDate,item);

    }


    @Test
    public void nowWithFormat() throws JsonProcessingException {

        Date module=new Date();
        module.setScenario(scenario);

        HashMap params=new HashMap();
        params.put("format","yyyyMMdd");

        Map ret=module.now(params);

        String pattern = "yyyyMMdd";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern(pattern);
        String formattedDate = now.format(myFormatObj);

        String item=(String)ret.get("result");
        Assert.assertEquals(formattedDate,item);

    }

}
