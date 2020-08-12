package dev.memestudio.framework.security.context;

import lombok.NonNull;

/**
 * @author meme
 * @since 2020/8/8
 */
public interface ResourceAccessProvider {

    ResourceAccess<? extends AccessType> provide(@NonNull String userId);

}
