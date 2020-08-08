package dev.memestudio.framework.security.user;

import lombok.Value;

import java.util.Set;

/**
 * @author meme
 * @since 2020/8/7
 */
@Value
public class CurrentAuthUser {

    String userId;

    String username;

    Set<String> permissions;

}
