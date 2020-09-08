package dev.memestudio.framework.security.auth.server;

import dev.memestudio.framework.redis.RedisOps;
import dev.memestudio.framework.security.auth.server.auth.AuthTokenHandler;
import dev.memestudio.framework.security.auth.server.auth.AuthTokenStore;
import dev.memestudio.framework.security.auth.server.auth.TokenScope;
import dev.memestudio.framework.security.auth.server.auth.TokenToUserIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.List;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author meme
 * @since 2020/8/15
 */
@EnableWebFlux
@Configuration
public class FrameworkAuthServerAutoConfiguration implements WebFluxConfigurer {

    @Bean
    public TokenToUserIdFilter tokenToUserIdFilter(AuthTokenStore authTokenStore) {
        return new TokenToUserIdFilter(authTokenStore);
    }

    @Bean
    public AuthTokenHandler authTokenHandler(AuthTokenStore authTokenStore, List<TokenScope> tokenScopes) {
        return new AuthTokenHandler(authTokenStore, tokenScopes.stream()
                                                               .collect(toMap(TokenScope::getName, identity())));
    }

    @Bean
    public AuthTokenStore authTokenStore(RedisOps redisOps) {
        return new AuthTokenStore(redisOps);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/token/**")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("*");
    }

}
