package dev.memestudio.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.security.context.AccessTypeHeaderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

//@EnableConfigurationProperties(FrameworkSecurityProperties.class)
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

    @Lazy
    @Bean
    public AuthUserInterceptor authUserInterceptor(AccessTypeHeaderMapper accessTypeHeaderMapper) {
        return new AuthUserInterceptor(accessTypeHeaderMapper);
    }

    @Bean
    public AuthUserIdMethodArgumentResolver authenticationUserIdMethodArgumentResolver() {
        return new AuthUserIdMethodArgumentResolver(objectMapper);
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

    @ConditionalOnMissingBean
    @Bean
    public AccessTypeHeaderMapper accessTypeHeaderMapper() {
        return new AccessTypeHeaderMapper();
    }


}