package dev.memestudio.framework.security.permission;

import java.lang.annotation.*;

/**
 * @author meme
 * @since 2020/8/1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnPermission {

    String value();

    String description();

}
