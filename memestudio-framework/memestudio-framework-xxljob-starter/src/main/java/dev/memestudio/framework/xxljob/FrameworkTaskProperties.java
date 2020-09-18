package dev.memestudio.framework.xxljob;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 *
 * @author meme
 * @since 2019-03-04 18:05
 */
@Getter
@Setter
@ConfigurationProperties(prefix = FrameworkTaskProperties.PREFIX)
class FrameworkTaskProperties {

    public static final String PREFIX = "memestudio-framework.task";

    @Value("${spring.application.name}")
    private String appName;

    private String accessToken;

    private List<String> executors;

    private int logRetentionDays = 30;


}
