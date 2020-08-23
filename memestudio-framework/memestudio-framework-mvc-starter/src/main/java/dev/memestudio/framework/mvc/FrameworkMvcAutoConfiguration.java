package dev.memestudio.framework.mvc;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.mvc.doc.FrameworkSwaggerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.stream.Collectors;

/**
 * @author meme
 * @since 2020/7/21
 */
@RequiredArgsConstructor
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@Configuration
public class FrameworkMvcAutoConfiguration {

    private final ServerProperties serverProperties;

    @Bean
    @Order(Integer.MIN_VALUE)
    public ErrorAttributes errorAttributes(ObjectMapper objectMapper,
                                           @Value("${spring.application.name}") String appName, Tracer tracer) {
        return new CommonErrorAttributes(objectMapper, appName, tracer);
    }

    @Bean
    public CommonErrorController basicErrorController(ErrorAttributes errorAttributes,
                                                      ObjectProvider<ErrorViewResolver> errorViewResolvers) {
        return new CommonErrorController(errorAttributes, this.serverProperties.getError(),
                errorViewResolvers.orderedStream().collect(Collectors.toList()));
    }

    @Bean
    public CommonExceptionHandler commonExceptionHandler(@Value("${spring.application.name}") String appName, Tracer tracer) {
        return new CommonExceptionHandler(appName, tracer);
    }

    @EnableConfigurationProperties(FrameworkSwaggerProperties.class)
    @ConditionalOnProperty(prefix = FrameworkSwaggerProperties.PREFIX, value = "enabled")
    @Configuration
    @EnableSwagger2
    @Import(BeanValidatorPluginsConfiguration.class)
    public static class FrameworkSwaggerConfiguration {


        @Bean
        @Order(value = 1)
        public Docket docket(FrameworkSwaggerProperties properties) {
            return new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(apiInfo(properties))
                    .select()
                        .apis(RequestHandlerSelectors.basePackage(properties.getBasePackage()))
                    .paths(PathSelectors.any())
                    .build();
        }

        private ApiInfo apiInfo(FrameworkSwaggerProperties properties){
            return new ApiInfoBuilder()
                    .title(properties.getTitle())
                    .description(properties.getDescription())
                    .version(properties.getVersion())
                    .build();
        }

    }
}
