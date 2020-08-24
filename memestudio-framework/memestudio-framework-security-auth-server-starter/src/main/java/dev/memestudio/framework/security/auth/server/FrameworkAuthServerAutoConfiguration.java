package dev.memestudio.framework.security.auth.server;

import dev.memestudio.framework.redis.RedisOps;
import dev.memestudio.framework.security.auth.server.token.AuthTokenHandler;
import dev.memestudio.framework.security.auth.server.token.AuthTokenStore;
import dev.memestudio.framework.security.auth.server.token.TokenToUserIdFilter;
import dev.memestudio.framework.security.context.UserIdService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.Map;

/**
 * @author meme
 * @since 2020/8/15
 */
@EnableWebFlux
@EnableConfigurationProperties(FrameworkAuthServerProperties.class)
@Configuration
public class FrameworkAuthServerAutoConfiguration implements WebFluxConfigurer {

    @Bean
    public TokenToUserIdFilter tokenToUserIdFilter(AuthTokenStore authTokenStore) {
        return new TokenToUserIdFilter(authTokenStore);
    }

    @Bean
    public AuthTokenHandler authTokenHandler(AuthTokenStore authTokenStore, Map<String, UserIdService> userIdServices) {
        return new AuthTokenHandler(authTokenStore, userIdServices);
    }

    @Bean
    public AuthTokenStore authTokenStore(RedisOps redisOps, FrameworkAuthServerProperties properties) {
        return new AuthTokenStore(redisOps, properties.getTokenTimeout(), properties.getRefreshTokenTimeout());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/token/**")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("*");
    }

}
