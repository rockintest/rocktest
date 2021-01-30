package io.rocktest.modules;

import io.rocktest.RockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class Time extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Time.class);

    public Map<String,Object> now(Map<String,Object> params) {

        String pattern = getStringParam(params,"format","HH:mm:ss");
        String timeZone = getStringParam(params,"timeZone",null);

        ZoneId z;
        if(timeZone==null) {
            z=ZoneId.systemDefault();
        } else {
            z=ZoneId.of(timeZone);
        }

        OffsetDateTime now = OffsetDateTime.ofInstant(Instant.now(), z);

        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern(pattern);

        String formattedTime = now.format(myFormatObj);

        Map<String,Object> ret=new HashMap<>();

        ret.put("result",formattedTime);

        return ret;
    }




    public Map<String,Object> minus(Map<String,Object> params) {

        String pattern = getStringParam(params, "format", "HH:mm:ss");
        String start = getStringParam(params, "time", null);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        Integer hours = getIntParam(params, "hours", null);
        Integer minutes = getIntParam(params, "minutes", null);
        Integer seconds = getIntParam(params, "seconds", null);
        Integer millis = getIntParam(params, "millis", null);

        if (start == null) {
            LocalTime now = LocalTime.now();
            start = now.format(formatter);
        }

        LocalTime time;

        try {
            time = LocalTime.parse(start, formatter);
        } catch (DateTimeParseException e) {
            throw new RockException("Error parsing date",e);
        }

        if (hours != null)
            time = time.minus(hours, ChronoUnit.HOURS);
        if (minutes != null)
            time = time.minus(minutes, ChronoUnit.MINUTES);
        if (seconds != null)
            time = time.minus(seconds, ChronoUnit.SECONDS);
        if (millis != null)
            time = time.minus(millis, ChronoUnit.MILLIS);

        String formattedTime = time.format(formatter);

        Map<String, Object> ret = new HashMap<>();

        ret.put("result", formattedTime);

        return ret;
    }


    public Map<String,Object> plus(Map<String,Object> params) {

        String pattern = getStringParam(params, "format", "HH:mm:ss");
        String start = getStringParam(params, "time", null);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        Integer hours = getIntParam(params, "hours", null);
        Integer minutes = getIntParam(params, "minutes", null);
        Integer seconds = getIntParam(params, "seconds", null);
        Integer millis = getIntParam(params, "millis", null);

        if (start == null) {
            LocalTime now = LocalTime.now();
            start = now.format(formatter);
        }

        LocalTime time;

        try {
            time = LocalTime.parse(start, formatter);
        } catch (DateTimeParseException e) {
            throw new RockException("Error parsing date",e);
        }

        if (hours != null)
            time = time.plus(hours, ChronoUnit.HOURS);
        if (minutes != null)
            time = time.plus(minutes, ChronoUnit.MINUTES);
        if (seconds != null)
            time = time.plus(seconds, ChronoUnit.SECONDS);
        if (millis != null)
            time = time.plus(millis, ChronoUnit.MILLIS);

        String formattedTime = time.format(formatter);

        Map<String, Object> ret = new HashMap<>();

        ret.put("result", formattedTime);

        return ret;
    }


}
