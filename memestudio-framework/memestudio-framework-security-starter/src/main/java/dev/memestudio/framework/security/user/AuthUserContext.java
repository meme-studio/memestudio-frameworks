package dev.memestudio.framework.security.user;

import dev.memestudio.framework.common.error.BusinessException;
import dev.memestudio.framework.redis.RedisOps;
import dev.memestudio.framework.security.AuthErrorCode;
import dev.memestudio.framework.security.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;
import java.util.stream.Collectors;

import static io.vavr.API.Option;
import static io.vavr.API.unchecked;

/**
 * @author meme
 * @since 2020/8/1
 */
@RequiredArgsConstructor
public class AuthUserContext {

    private final RedisOps redisOps;

    private final ObjectMapper objectMapper;

    public AuthUser current() {
        return Option(RequestContextHolder.getRequestAttributes())
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(request -> request.getHeader(AuthUserConstants.AUTH_USER_HEADER))
                .map(unchecked(user -> objectMapper.readValue(user, AuthUser.class)))
                .peek(this::setPermissions)
                .getOrNull();
    }

    @SuppressWarnings("unchecked")
    private void setPermissions(AuthUser user) {
        redisOps.hGet(AuthUserConstants.AUTH_USER_PERMISSIONS, user.getUserId(), Set.class)
                .map(permissions -> ((Set<String>) permissions))
                .map(permissions -> permissions.stream()
                                               .map(Permission::new)
                                               .collect(Collectors.toSet()))
                .orElseThrow(() -> new BusinessException(AuthErrorCode.EMPTY_PERMISSIONS));
    }


}
