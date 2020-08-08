package dev.memestudio.framework.security.permission;

import dev.memestudio.framework.redis.RedisOps;
import dev.memestudio.framework.security.user.AuthUserConstants;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;

/**
 * @author meme
 * @since 2020/8/8
 */
@RequiredArgsConstructor
public class PermissionHolder {

    private final RedisOps redisOps;

    public void hold(@NonNull Object userId, Set<String> permissions) {
        redisOps.hSet(AuthUserConstants.AUTH_USER_PERMISSIONS, String.valueOf(userId), permissions);
    }

    @SuppressWarnings("rawtypes")
    public Optional<Set> get(@NonNull Object userId) {
        return redisOps.hGet(AuthUserConstants.AUTH_USER_PERMISSIONS, String.valueOf(userId), Set.class);
    }

    public void clear(@NotNull Object userId) {
        redisOps.hDel(AuthUserConstants.AUTH_USER_PERMISSIONS, String.valueOf(userId));
    }



}
