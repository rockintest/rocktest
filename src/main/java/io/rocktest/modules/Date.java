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

public class Date extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Date.class);

    public Map<String,Object> now(Map<String,Object> params) {

        String pattern = getStringParam(params,"format","dd/MM/yyyy");
        String timeZone = getStringParam(params,"timeZone",null);

        ZoneId z;
        if(timeZone==null) {
            z=ZoneId.systemDefault();
        } else {
            z=ZoneId.of(timeZone);
        }

        OffsetDateTime now = OffsetDateTime.ofInstant(Instant.now(), z);

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

            String pattern = getStringParam(params, "format", "dd/MM/yyyy");
            String start = getStringParam(params, "date", null);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

            Integer days = getIntParam(params, "days", null);
            Integer months = getIntParam(params, "months", null);
            Integer years = getIntParam(params, "years", null);

            Integer hours = getIntParam(params, "hours", null);
            Integer minutes = getIntParam(params, "minutes", null);
            Integer seconds = getIntParam(params, "seconds", null);
            Integer millis = getIntParam(params, "millis", null);

            if (start == null) {
                LocalDateTime now = LocalDateTime.now();
                start = now.format(formatter);
            }

            LocalDateTime dateTime;

            try {
                dateTime = LocalDateTime.parse(start, formatter);
            } catch (DateTimeParseException e) {
                try {
                    dateTime = LocalDate.parse(start, formatter).atStartOfDay();
                } catch (DateTimeParseException e2) {
                    throw new RockException("Error parsing date",e2);
                }
            }
            if (years != null)
                dateTime = dateTime.minus(years, ChronoUnit.YEARS);
            if (months != null)
                dateTime = dateTime.minus(months, ChronoUnit.MONTHS);
            if (days != null)
                dateTime = dateTime.minus(days, ChronoUnit.DAYS);
            if (hours != null)
                dateTime = dateTime.minus(hours, ChronoUnit.HOURS);
            if (minutes != null)
                dateTime = dateTime.minus(minutes, ChronoUnit.MINUTES);
            if (seconds != null)
                dateTime = dateTime.minus(seconds, ChronoUnit.SECONDS);
            if (millis != null)
                dateTime = dateTime.minus(millis, ChronoUnit.MILLIS);


            String formattedDate = dateTime.format(formatter);

            Map<String, Object> ret = new HashMap<>();

            ret.put("result", formattedDate);

            return ret;
    }


    public Map<String,Object> plus(Map<String,Object> params) {

        String pattern = getStringParam(params, "format", "dd/MM/yyyy");
        String start = getStringParam(params, "date", null);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        Integer days = getIntParam(params, "days", null);
        Integer months = getIntParam(params, "months", null);
        Integer years = getIntParam(params, "years", null);

        Integer hours = getIntParam(params, "hours", null);
        Integer minutes = getIntParam(params, "minutes", null);
        Integer seconds = getIntParam(params, "seconds", null);
        Integer millis = getIntParam(params, "millis", null);

        if (start == null) {
            LocalDateTime now = LocalDateTime.now();
            start = now.format(formatter);
        }

        LocalDateTime dateTime;

        try {
            dateTime = LocalDateTime.parse(start, formatter);
        } catch (DateTimeParseException e) {
            try {
                dateTime = LocalDate.parse(start, formatter).atStartOfDay();
            } catch (DateTimeParseException e2) {
                throw new RockException("Error parsing date",e2);
            }
        }
        if (years != null)
            dateTime = dateTime.plus(years, ChronoUnit.YEARS);
        if (months != null)
            dateTime = dateTime.plus(months, ChronoUnit.MONTHS);
        if (days != null)
            dateTime = dateTime.plus(days, ChronoUnit.DAYS);
        if (hours != null)
            dateTime = dateTime.plus(hours, ChronoUnit.HOURS);
        if (minutes != null)
            dateTime = dateTime.plus(minutes, ChronoUnit.MINUTES);
        if (seconds != null)
            dateTime = dateTime.plus(seconds, ChronoUnit.SECONDS);
        if (millis != null)
            dateTime = dateTime.plus(millis, ChronoUnit.MILLIS);


        String formattedDate = dateTime.format(formatter);

        Map<String, Object> ret = new HashMap<>();

        ret.put("result", formattedDate);

        return ret;
    }


}
