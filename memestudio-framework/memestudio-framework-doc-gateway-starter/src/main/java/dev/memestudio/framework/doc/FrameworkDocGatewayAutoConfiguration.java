package dev.memestudio.framework.doc;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * @author meme
 * @since 2020/8/17
 */
@Configuration
public class FrameworkDocGatewayAutoConfiguration {

    @Bean
    public SwaggerHandler swaggerHandler(SwaggerResourcesProvider swaggerResourcesProvider) {
        return new SwaggerHandler(swaggerResourcesProvider);
    }

    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider(RouteLocator routeLocator, GatewayProperties gatewayProperties) {
        return new FrameworkSwaggerResourcesProvider(routeLocator, gatewayProperties);
    }

    @Bean
    public SwaggerHeaderFilter swaggerHeaderFilter() {
        return new SwaggerHeaderFilter();
    }

}
