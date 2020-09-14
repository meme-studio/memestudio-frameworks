package dev.memestudio.framework.client;

import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author meme
 * @since 2020/9/14
 */
public class DateToStringFormatter implements FeignFormatterRegistrar {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(Date.class, String.class, new DateToStringConverter());
    }
    private static class DateToStringConverter implements Converter<Date, String> {
        @Override
        public String convert(Date source) {
            DateFormat dateFormat = new SimpleDateFormat(PATTERN);
            return dateFormat.format(source);
        }
    }
}
