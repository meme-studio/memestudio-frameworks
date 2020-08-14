package dev.memestudio.framework.security.context;

import dev.memestudio.framework.common.error.BusinessException;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author meme
 * @since 2020/8/7
 */
@Value
public class CurrentAuthUser {

    String userId;

    Set<String> permissions;

    ResourceAccess resourceAccess;

    Map<AccessType, String> currentResourceAccess;

    public CurrentAuthUser(String userId, Set<String> permissions, ResourceAccess resourceAccess, Map<AccessType, String> currentResourceAccess) {
        this.userId = userId;
        this.permissions = permissions;
        this.resourceAccess = resourceAccess;
        currentResourceAccess.entrySet()
                             .stream()
                             .filter(entry -> hasResource(entry.getKey(), entry.getValue()))
                             .findAny()
                             .orElseThrow(() -> new BusinessException(AuthErrorCode.NO_RESOURCE_ACCESS));
        this.currentResourceAccess = currentResourceAccess;
    }
    public String currentResourceAccess(AccessType type) {
        return currentResourceAccess.get(type);
    }

    public boolean hasResource(AccessType type, String resourceId) {
        List<String> resourceIds = resourceAccess.listResourceIds(type);
        return Objects.isNull(resourceIds) || resourceIds.contains(resourceId);
    }

    public boolean hasPermission(String permission) {
        return Objects.isNull(permissions) || permissions.contains(permission);
    }

}
