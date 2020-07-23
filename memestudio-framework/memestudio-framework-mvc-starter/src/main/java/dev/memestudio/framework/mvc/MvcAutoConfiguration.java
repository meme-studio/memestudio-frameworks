package dev.memestudio.framework.mvc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author meme
 * @since 2020/7/21
 */
@Configuration
public class MvcAutoConfiguration {

    @Bean
    public ExceptionHandlers exceptionHandlers(@Value("${spring.application.name}") String appName) {
        return new ExceptionHandlers(appName);
    }

}
