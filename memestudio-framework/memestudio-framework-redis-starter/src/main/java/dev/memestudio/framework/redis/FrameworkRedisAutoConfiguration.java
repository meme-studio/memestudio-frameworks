package dev.memestudio.framework.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.common.support.NumericIdGenerator;
import dev.memestudio.framework.redis.support.RedisNumericIdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;

/**
 * @author meme
 * @since 2020/7/13
 */
@EnableConfigurationProperties(FrameworkRedisProperties.class)
@Configuration
public class FrameworkRedisAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public NumericIdGenerator defaultIdGenerator(RedisOps redisOps) {
        return new RedisNumericIdGenerator(redisOps);
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
}
