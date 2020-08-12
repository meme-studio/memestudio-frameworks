package dev.memestudio.framework.security.context;

import lombok.NonNull;

import java.util.Set;

/**
 * @author meme
 * @since 2020/8/8
 */
public interface PermissionProvider {

    Set<String> provide(@NonNull String userId);

}
