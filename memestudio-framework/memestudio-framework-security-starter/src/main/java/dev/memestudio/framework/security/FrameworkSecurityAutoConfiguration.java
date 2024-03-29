package dev.memestudio.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.security.context.AccessTypeHeaderMapper;
import dev.memestudio.framework.security.context.PermissionProvider;
import dev.memestudio.framework.security.context.ResourceAccessProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@ConditionalOnWebApplication
@Configuration
public class FrameworkSecurityAutoConfiguration {

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

    @ConditionalOnMissingBean
    @Bean
    public AccessTypeHeaderMapper accessTypeHeaderMapper() {
        return new AccessTypeHeaderMapper();
    }

    @ConditionalOnMissingBean
    @Bean
    public PermissionProvider permissionProvider() {
        return __ -> null;
    }

    @ConditionalOnMissingBean
    @Bean
    public ResourceAccessProvider resourceAccessProvider() {
        return __ -> null;
    }

    @RequiredArgsConstructor
    @Configuration
    static class SecurityWebMvcConfigurer implements WebMvcConfigurer {

        private final AuthUserInterceptor authUserInterceptor;

        private final PermissionInterceptor permissionInterceptor;

        private final ObjectMapper objectMapper;

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(authenticationUserIdMethodArgumentResolver());
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(authUserInterceptor).addPathPatterns("/**");
            registry.addInterceptor(permissionInterceptor).addPathPatterns("/**");
        }

        @Bean
        public AuthUserIdMethodArgumentResolver authenticationUserIdMethodArgumentResolver() {
            return new AuthUserIdMethodArgumentResolver(objectMapper);
        }

    }

}