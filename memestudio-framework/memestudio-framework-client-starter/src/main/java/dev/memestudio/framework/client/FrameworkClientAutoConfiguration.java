package dev.memestudio.framework.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.client.error.ClientErrorDecoder;
import dev.memestudio.framework.client.error.ClientExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.stream.Collectors;

/**
 * @author meme
 * @since 2020/8/24
 */
@EnableFeignClients(basePackages = "${memestudio-framework.client.base-packages}")
@Configuration
public class FrameworkClientAutoConfiguration {

    @Bean
    public ClientExceptionHandler clientExceptionHandler() {
        return new ClientExceptionHandler();
    }

    @Bean
    public ClientErrorDecoder clientErrorDecoder(ObjectMapper objectMapper) {
        return new ClientErrorDecoder(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }

    @Bean
    public DateToStringFormatter dateToStringFormatter() {
        return new DateToStringFormatter();
    }

}
