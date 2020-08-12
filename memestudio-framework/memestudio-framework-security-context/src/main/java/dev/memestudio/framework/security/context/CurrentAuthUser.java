package dev.memestudio.framework.security.context;

import lombok.Data;

import java.util.Set;

/**
 * @author meme
 * @since 2020/8/7
 */
@Data
public class CurrentAuthUser {

    private String userId;

    private Set<String> permissions;

    private ResourceAccess<? extends AccessType> resourceAccess;

}
