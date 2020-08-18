package dev.memestudio.framework.mvc.doc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author meme
 * @since 2020/7/21
 */
@Getter
@Setter
@ConfigurationProperties(prefix = FrameworkSwaggerProperties.PREFIX)
public class FrameworkSwaggerProperties {

    public static final String PREFIX = "memestudio-framework.doc";

    private String basePackage;

    private String title;

    private String description;

    private String version;

    private Boolean enabled;

}
