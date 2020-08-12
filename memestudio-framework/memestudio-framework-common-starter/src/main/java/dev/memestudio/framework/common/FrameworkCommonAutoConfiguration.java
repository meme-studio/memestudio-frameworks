package dev.memestudio.framework.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.common.time.DateProvider;
import dev.memestudio.framework.common.time.DateTimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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


}