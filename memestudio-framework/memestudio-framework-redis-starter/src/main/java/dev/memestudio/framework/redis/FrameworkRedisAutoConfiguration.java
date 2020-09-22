package dev.memestudio.framework.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alturkovic.lock.redis.configuration.EnableRedisDistributedLock;
import dev.memestudio.framework.common.support.NumericIdGenerator;
import dev.memestudio.framework.redis.support.RedisNumericIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

/**
 * @author meme
 * @since 2020/7/13
 */
@AutoConfigureBefore(name = {"org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration", "org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration"})
@EnableRedisDistributedLock
@EnableCaching
@EnableConfigurationProperties(FrameworkRedisProperties.class)
@Configuration
public class FrameworkRedisAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public NumericIdGenerator defaultIdGenerator(RedisOps redisOps, @Value("${spring.application.name}") String key) {
        return new RedisNumericIdGenerator(redisOps, key);
    }

    @Bean
    public RedisOps redisOps(
            StringRedisTemplate template,
            ObjectMapper mapper,
            FrameworkRedisProperties properties) {
        return new RedisOps(template, mapper, properties.getKeyPrefix());
    }

    @Bean
    public LockRegistry lockRegistry(
            RedisConnectionFactory connectionFactory,
            FrameworkRedisProperties properties) {
        return new RedisLockRegistry(connectionFactory, String.format("%s:lock", properties.getKeyPrefix()), properties.getLock().getExpireAfter());
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory,
                                     FrameworkRedisProperties properties) {
        return RedisCacheManager.builder(factory)
                                .transactionAware()
                                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                                                                      .prefixCacheNameWith(String.format("%s:", properties.getKeyPrefix()))
                                                                      .serializeKeysWith(fromSerializer(RedisSerializer.string()))
                                                                      .serializeValuesWith(fromSerializer(RedisSerializer.json())))
                                .build();
    }
}
