package dev.memestudio.framework.mvc;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author meme
 * @since 2020/7/21
 */
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@Configuration
public class MvcAutoConfiguration {

    @Bean
    @Order(Integer.MIN_VALUE)
    public ErrorAttributes errorAttributes(ObjectMapper objectMapper,
                                           @Value("${spring.application.name}") String appName, Tracer tracer) {
        return new CommonErrorAttributes(objectMapper, appName, tracer);
    }

}
