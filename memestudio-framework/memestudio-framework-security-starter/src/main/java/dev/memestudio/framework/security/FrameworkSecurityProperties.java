package dev.memestudio.framework.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * @author meme
 * @since 2020/8/1
 */
@Getter
@Setter
@ConfigurationProperties(prefix = FrameworkSecurityProperties.PREFIX)
public class FrameworkSecurityProperties {

    static final String PREFIX = "memestudio-framework.security";

    private Mode mode = Mode.SERVICE;

    private String appId;

    private JksKeyPair jksKeyPair = new JksKeyPair();

    @Getter
    @Setter
    static class JksKeyPair {

        private Resource path;

        private String alias;

        private String password;
    }

    enum Mode {
        GATEWAY, SERVICE, DEBUG
    }

}
