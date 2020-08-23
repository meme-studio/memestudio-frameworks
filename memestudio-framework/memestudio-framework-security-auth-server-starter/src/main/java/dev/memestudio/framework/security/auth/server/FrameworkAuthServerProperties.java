package dev.memestudio.framework.security.auth.server;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author meme
 * @since 2020/8/23
 */
@Getter
@Setter
@ConfigurationProperties(prefix = FrameworkAuthServerProperties.PREFIX)
public class FrameworkAuthServerProperties {
    public static final String PREFIX = "memestudio-framework.auth-server";

    private long tokenTimeout = 2 * 60 * 60;

    private long refreshTokenTimeout = 14 * 24 * 60 *60;
}
