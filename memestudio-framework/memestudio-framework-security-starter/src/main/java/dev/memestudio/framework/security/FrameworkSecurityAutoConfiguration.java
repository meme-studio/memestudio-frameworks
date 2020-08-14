package dev.memestudio.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.security.context.AccessTypeHeaderMapper;
import dev.memestudio.framework.security.context.PermissionProvider;
import dev.memestudio.framework.security.context.ResourceAccessProvider;
import feign.Feign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@ConditionalOnWebApplication
@Configuration
public class FrameworkSecurityAutoConfiguration implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthUserInterceptor authUserInterceptor;

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Bean
    public PermissionInterceptor permissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Bean
    public AuthUserInterceptor authUserInterceptor(PermissionProvider permissionProvider,
                                                   ResourceAccessProvider resourceAccessProvider,
                                                   AccessTypeHeaderMapper accessTypeHeaderMapper) {
        return new AuthUserInterceptor(permissionProvider, resourceAccessProvider, accessTypeHeaderMapper);
    }

    @Bean
    public AuthUserIdMethodArgumentResolver authenticationUserIdMethodArgumentResolver() {
        return new AuthUserIdMethodArgumentResolver(objectMapper);
    }

    @ConditionalOnClass(Feign.class)
    @Bean
    public AuthUserClientRequestInterceptor authUserClientRequestInterceptor() {
        return new AuthUserClientRequestInterceptor();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(authenticationUserIdMethodArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authUserInterceptor).addPathPatterns("/**");
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/**");
    }
}