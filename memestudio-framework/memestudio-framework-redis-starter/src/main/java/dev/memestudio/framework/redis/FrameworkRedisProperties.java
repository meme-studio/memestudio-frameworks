package dev.memestudio.framework.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author meme
 * @since 2020/7/21
 */
@Getter
@Setter
@ConfigurationProperties(prefix = FrameworkRedisProperties.PREFIX)
public class FrameworkRedisProperties {

    static final String PREFIX = "memestudio-framework.redis";

    /**
     * 应用前缀
     */
    @Value("${spring.application.name}")
    private String keyPrefix;

    private Lock lock = new Lock();

    @Getter
    @Setter
    public static class Lock {

        /**
         * 锁失效时间
         */
        private long expireAfter = 60;

    }

}
