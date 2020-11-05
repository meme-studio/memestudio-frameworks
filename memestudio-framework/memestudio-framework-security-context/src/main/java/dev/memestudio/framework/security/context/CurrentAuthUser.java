package dev.memestudio.framework.security.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.apache.commons.collections4.MapUtils;

import java.util.*;

import static java.util.function.Predicate.isEqual;

/**
 * @author meme
 * @since 2020/8/7
 */
@Value
public class CurrentAuthUser {

    String userId;

    Set<String> permissions;

    ResourceAccess resourceAccess;

    @Getter(AccessLevel.PRIVATE)
    Map<AccessType, String> currentResourceAccess;

    public CurrentAuthUser(String userId, Set<String> permissions, ResourceAccess resourceAccess, Map<AccessType, String> currentResourceAccess) {
        this.userId = userId;
        this.permissions = permissions;
        this.resourceAccess = resourceAccess;
        Optional.of(currentResourceAccess)
                .filter(MapUtils::isNotEmpty)
                .ifPresent(__ -> currentResourceAccess.entrySet()
                                                      .stream()
                                                      .filter(entry -> hasResource(entry.getKey(), entry.getValue()))
                                                      .findAny()
                                                      .orElseThrow(() -> new AuthException(AuthErrorCode.NO_RESOURCE_ACCESS)));

        this.currentResourceAccess = currentResourceAccess;
    }

    public String currentResourceAccess(AccessType type) {
        return currentResourceAccess.get(type);
    }

    public boolean hasResource(AccessType type, String resourceId) {
        List<String> resourceIds = resourceAccess.listResourceIds(type);
        return Objects.isNull(resourceIds) || resourceIds.contains(resourceId);
    }

    public boolean hasPermission(String[] checkingPermissions) {
        return Objects.isNull(permissions) || Arrays.stream(checkingPermissions)
                                                    .anyMatch(checkingPermission -> permissions.stream()
                                                                                               .anyMatch(isEqual(checkingPermission)));
    }

}
