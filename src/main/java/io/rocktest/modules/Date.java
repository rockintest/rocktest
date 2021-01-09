package io.rocktest.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class Date extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Date.class);

    public Map<String,Object> now(Map<String,Object> params) {

        String pattern = getStringParam(params,"format","dd/MM/yyyy");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern(pattern);

        String formattedDate = now.format(myFormatObj);

        Map<String,Object> ret=new HashMap<>();

        ret.put("result",formattedDate);

        return ret;
    }


    /**
     * @param params
     *
     * Paramters :
     * <ul>
     *     <li>format</li>
     *     <li>date</li>
     * </ul>
     *
     * @return
     */
    public Map<String,Object> minus(Map<String,Object> params) {

        String pattern = getStringParam(params,"format","dd/MM/yyyy");
        String start = getStringParam(params,"date",null);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        Integer days = getIntParam(params,"days",null);
        Integer months = getIntParam(params,"months",null);
        Integer years = getIntParam(params,"years",null);

        if(start==null) {
            LocalDateTime now = LocalDateTime.now();
            start = now.format(formatter);
        }

        LocalDateTime dateTime;

        try {
            dateTime = LocalDateTime.parse(start,formatter);
        } catch(DateTimeParseException e) {
            dateTime = LocalDate.parse(start,formatter).atStartOfDay();
        }
        if(years!=null)
            dateTime = dateTime.minus(years, ChronoUnit.YEARS);
        if(months!=null)
            dateTime = dateTime.minus(months, ChronoUnit.MONTHS);
        if(days!=null)
            dateTime = dateTime.minus(days, ChronoUnit.DAYS);

        String formattedDate = dateTime.format(formatter);

        Map<String,Object> ret=new HashMap<>();

        ret.put("result",formattedDate);

        return ret;
    }

}
