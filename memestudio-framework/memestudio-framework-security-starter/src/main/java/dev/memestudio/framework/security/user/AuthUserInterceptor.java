package dev.memestudio.framework.security.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.security.permission.PermissionHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;

/**
 * @author meme
 * @since 2020/8/1
 */
@RequiredArgsConstructor
public class AuthUserInterceptor extends HandlerInterceptorAdapter {

    private final PermissionHolder permissionHolder;

    private final ObjectMapper objectMapper;

    private final AuthUserResolver<Object, Object> authUserResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Optional.ofNullable(request.getHeader(AuthUserConstants.AUTH_USER_HEADER))
                .ifPresent(this::determineAuthUser);
        return true;
    }

    @SneakyThrows
    private void determineAuthUser(String authUserString) {
        AuthUserInfo authUser = objectMapper.readValue(authUserString, AuthUserInfo.class);
        CurrentAuthUser currentAuthUser = new CurrentAuthUser(authUser.getUserId(), authUser.getUsername(), getPermissions(authUser));
        AuthUserContext.setCurrent(currentAuthUser);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        AuthUserContext.reset();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthUserContext.reset();
    }

    @SuppressWarnings("unchecked")
    private Set<String> getPermissions(AuthUserInfo user) {
        return permissionHolder.get(user.getUserId())
                               .map(permissions -> ((Set<String>) permissions))
                               .orElseGet(() -> getPermissionFromAuthUserResolver(user));
    }

    private Set<String> getPermissionFromAuthUserResolver(AuthUserInfo user) {
        Set<String> permissions = authUserResolver.determinePermission(user.getUserId());
        permissionHolder.hold(user.getUserId(), permissions);
        return permissions;
    }


}
