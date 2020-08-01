package dev.memestudio.framework.security.permission;

import dev.memestudio.framework.security.user.AuthUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author meme
 * @since 2020/8/1
 */
@RequiredArgsConstructor
public class PermissionInterceptor extends HandlerInterceptorAdapter {

    private final AuthUserContext authUserContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return authUserContext.current()
                              .listPermissions()
                              .stream()
                              .anyMatch(this::check);
    }

    private boolean check(String permission) {
        return false;
    }

}
