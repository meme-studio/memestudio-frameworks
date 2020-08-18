package dev.memestudio.framework.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.common.time.DateProvider;
import dev.memestudio.framework.common.time.DateTimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.stream.Collectors;

@Slf4j
@Configuration
public class FrameworkCommonAutoConfiguration {


    @Bean
    public DateProvider dateProvider() {
        return new DateProvider() {};
    }

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return new DateTimeProvider() {};
    }

    @Bean
    public FrameworkErrorDecoder frameworkErrorDecoder(ObjectMapper objectMapper) {
        return new FrameworkErrorDecoder(objectMapper);
    }


    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }

}