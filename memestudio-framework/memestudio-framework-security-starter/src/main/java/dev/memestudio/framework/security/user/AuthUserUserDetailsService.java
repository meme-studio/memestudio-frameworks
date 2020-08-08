package dev.memestudio.framework.security.user;

import dev.memestudio.framework.security.permission.PermissionHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

/**
 * @author meme
 * @since 2020/7/31
 */
@RequiredArgsConstructor
public class AuthUserUserDetailsService implements UserDetailsService {

    private final AuthUserResolver<Object, Object> authUserResolver;

    private final PermissionHolder permissionHolder;

    @Override
    public AuthUser loadUserByUsername(String username) throws UsernameNotFoundException {
        Object user = authUserResolver.getUser(username);
        AuthUserInfo info = authUserResolver.determineAuthUserInfo(user);
        AuthUser authUser = new AuthUser(info.getUserId(), info.getUsername(), info.getPassword());
        Object userId = authUserResolver.determineUserId(user);
        Set<String> permissions = authUserResolver.determinePermission(userId);
        permissionHolder.hold(userId, permissions);
        return authUser;
    }

}
