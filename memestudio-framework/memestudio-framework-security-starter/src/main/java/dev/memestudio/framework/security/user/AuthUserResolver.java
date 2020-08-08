package dev.memestudio.framework.security.user;

import java.util.Set;

/**
 * @author meme
 * @since 2020/8/8
 */
public interface AuthUserResolver<USER, ID> {

    /**
     * 获取认证用户信息
     */
    AuthUserInfo determineAuthUserInfo(USER user);

    /**
     * 获取用户权限
     */
    Set<String> determinePermission(ID userId);

    /**
     * 获取用户Id
     */
    ID determineUserId(USER user);

    /**
     * 获取用户
     *
     * @param condition 查询条件
     */
    USER getUser(Object condition);

}
