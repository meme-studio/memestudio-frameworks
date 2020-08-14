package dev.memestudio.framework.security.context;

import dev.memestudio.framework.common.error.BusinessException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

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

    @Getter
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
        List<String> resourceIds = resourceAccess.listResourceIds(type);
        return Objects.isNull(resourceIds) || resourceIds.contains(resourceId);
    }

    public boolean hasPermission(String permission) {
        return Objects.isNull(permissions) || permissions.contains(permission);
    }

}
