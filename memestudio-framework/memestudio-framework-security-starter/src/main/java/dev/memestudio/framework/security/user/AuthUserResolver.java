package dev.memestudio.framework.security.user;

import dev.memestudio.framework.redis.RedisOps;
import dev.memestudio.framework.security.permission.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author meme
 * @since 2020/7/31
 */
public abstract class AuthUserResolver<USER> implements UserDetailsService {

    @Autowired
    private RedisOps redisOps;

    @Override
    public AuthUser loadUserByUsername(String username) throws UsernameNotFoundException {
        USER user = getUser(username);
        AuthUserInfo info = determineAuthUserInfo(user);
        AuthUser authUser = new AuthUser(info.getUserId(), info.getUsername(), info.getPassword());
        Set<Permission> permissions = determinePermission(user).stream()
                                                           .map(Permission::new)
                                                           .collect(Collectors.toSet());
        authUser.setAuthorities(permissions);
        redisOps.hSet(AuthUserConstants.AUTH_USER_PERMISSIONS, authUser.getUserId(), permissions);
        return authUser;
    }

    /**
     * 获取认证用户信息
     */
    protected abstract AuthUserInfo determineAuthUserInfo(USER user);

    /**
     * 获取用户权限
     */
    protected abstract Set<String> determinePermission(USER user);

    /**
     * 获取用户
     *
     * @param condition 查询条件
     */
    protected abstract USER getUser(String condition);


}
