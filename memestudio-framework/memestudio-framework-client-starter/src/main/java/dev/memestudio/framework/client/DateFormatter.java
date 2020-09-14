package dev.memestudio.framework.client;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter implements Formatter<Date>, Converter<Date, String> {

    static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Date parse(String text, Locale locale) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(PATTERN);
        return dateFormat.parse(text);
    }

    @Override
    public String print(Date object, Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat(PATTERN);
        return dateFormat.format(object);
    }

    @Override
    public String convert(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(PATTERN);
        return dateFormat.format(date);
    }
}