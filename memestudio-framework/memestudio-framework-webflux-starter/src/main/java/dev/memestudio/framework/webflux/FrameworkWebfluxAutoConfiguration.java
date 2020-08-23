package dev.memestudio.framework.webflux;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.stream.Collectors;

/**
 * @author meme
 * @since 2020/7/21
 */
@RequiredArgsConstructor
@AutoConfigureBefore(ErrorWebFluxAutoConfiguration.class)
@Configuration
public class FrameworkWebfluxAutoConfiguration {

    private final ServerProperties serverProperties;


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorAttributes errorAttributes(ObjectMapper objectMapper,
                                           @Value("${spring.application.name}") String appName, Tracer tracer) {
        return new CommonErrorAttributes(objectMapper, appName, tracer);
    }

    @Bean
    @Order(-1)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
                                                             ResourceProperties resourceProperties, ObjectProvider<ViewResolver> viewResolvers,
                                                             ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
        CommonErrorWebExceptionHandler exceptionHandler = new CommonErrorWebExceptionHandler(errorAttributes,
                resourceProperties, this.serverProperties.getError(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    @Bean
    public CommonExceptionHandler commonExceptionHandler(@Value("${spring.application.name}") String appName, Tracer tracer) {
        return new CommonExceptionHandler(appName, tracer);
    }


}
