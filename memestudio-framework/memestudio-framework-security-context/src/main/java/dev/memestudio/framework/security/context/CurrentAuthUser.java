package dev.memestudio.framework.security.context;

import dev.memestudio.framework.common.error.BusinessException;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author meme
 * @since 2020/8/7
 */
public class CurrentAuthUser {

    @Getter
    @Setter
    private String userId;

    @Setter
    private Set<String> permissions;

    @Setter
    private ResourceAccess resourceAccess;

    private Map<AccessType, String> currentResourceAccess;

    public void setCurrentResourceAccess(Map<AccessType, String> currentResourceAccess) {
        boolean hasResourceAccess = currentResourceAccess.entrySet()
                                                         .stream()
                                                         .allMatch(entry -> hasResource(entry.getKey(), entry.getValue()));
        if (!hasResourceAccess) {
            throw new BusinessException(AuthErrorCode.NO_RESOURCE_ACCESS);
        }
        this.currentResourceAccess = currentResourceAccess;
    }

    public String currentResourceAccess(AccessType type) {
        return currentResourceAccess.get(type);
    }

    public boolean hasResource(AccessType type, String resourceId) {
        return Optional.ofNullable(resourceAccess.listResourceIds(type))
                       .map(resourceIds ->
                               resourceIds.contains(AuthConstants.AUTH_ALL_RESOURCE_ACCESS)
                                       || resourceIds.contains(resourceId))
                       .orElse(false);
    }

    public boolean hasPermission(String permission) {
        return Optional.ofNullable(permissions)
                       .map(__ ->
                               permissions.contains(AuthConstants.AUTH_USER_PERMISSIONS)
                                       || permissions.contains(permission))
                       .orElse(false);
    }

}
