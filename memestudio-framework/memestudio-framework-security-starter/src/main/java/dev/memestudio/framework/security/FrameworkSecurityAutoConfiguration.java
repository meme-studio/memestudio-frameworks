package dev.memestudio.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.redis.RedisOps;
import dev.memestudio.framework.security.permission.PermissionHolder;
import dev.memestudio.framework.security.user.*;
import feign.Feign;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.OAuth2AuthorizationServerConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author meme
 * @since 2020/7/31
 */
@AutoConfigureBefore(OAuth2AuthorizationServerConfiguration.class)
@ConditionalOnProperty(prefix = FrameworkSecurityProperties.PREFIX, name = "mode", havingValue = "SERVICE")
@EnableConfigurationProperties(FrameworkSecurityProperties.class)
@RequiredArgsConstructor
@Configuration
@EnableAuthorizationServer
public class FrameworkSecurityAutoConfiguration extends AuthorizationServerConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final AuthUserUserDetailsService authUserResolver;
    private final AuthenticationManager authenticationManager;
    private final FrameworkSecurityProperties properties;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
               .withClient(properties.getAppId())
               .secret(passwordEncoder.encode(properties.getAppId()))
               .scopes(properties.getAppId())
               .authorizedGrantTypes("password", "refresh_token")
               .accessTokenValiditySeconds(3600)
               .refreshTokenValiditySeconds(86400);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        enhancerChain.setTokenEnhancers(Arrays.asList((OAuth2AccessToken accessToken, OAuth2Authentication authentication) -> {
            AuthUser securityUser = (AuthUser) authentication.getPrincipal();
            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(Collections.singletonMap("userId", securityUser.getUserId()));
            return accessToken;
        }, accessTokenConverter())); //配置JWT的内容增强器
        endpoints.authenticationManager(authenticationManager)
                 .userDetailsService(authUserResolver) //配置加载用户信息的服务
                 .accessTokenConverter(accessTokenConverter())
                 .tokenEnhancer(enhancerChain);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients();
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyPair());
        return converter;
    }

    @Bean
    public KeyPair keyPair() {
        return new KeyStoreKeyFactory
                (properties.getJksKeyPair()
                           .getPath(),
                        properties.getJksKeyPair()
                                  .getPassword()
                                  .toCharArray())
                .getKeyPair(properties.getJksKeyPair()
                                      .getAlias());
    }

    @Bean
    public AuthUserUserDetailsService authUserUserDetailsService(AuthUserResolver<Object, Object> authUserResolver,
                                                                 PermissionHolder permissionHolder) {
        return new AuthUserUserDetailsService(authUserResolver, permissionHolder);
    }

    @Bean
    public PermissionHolder permissionHolder(RedisOps redisOps) {
        return new PermissionHolder(redisOps);
    }

    @ConditionalOnWebApplication
    @Configuration
    protected static class AuthenticationConfiguration implements WebMvcConfigurer {

        @Autowired
        private ObjectMapper objectMapper;

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
    }
}

