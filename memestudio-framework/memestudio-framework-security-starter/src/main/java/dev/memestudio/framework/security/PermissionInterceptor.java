package dev.memestudio.framework.security;

import dev.memestudio.framework.common.error.BusinessException;
import dev.memestudio.framework.security.context.AuthErrorCode;
import dev.memestudio.framework.security.context.AuthUserContext;
import dev.memestudio.framework.security.context.CurrentAuthUser;
import dev.memestudio.framework.security.context.OnPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * @author meme
 * @since 2020/8/1
 */
@RequiredArgsConstructor
public class PermissionInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Optional.of(handler)
                .filter(HandlerMethod.class::isInstance)
                .map(HandlerMethod.class::cast)
                .map(handlerMethod -> handlerMethod.getMethodAnnotation(OnPermission.class))
                .map(OnPermission::value)
                .ifPresent(this::check);
        return true;
    }

    private void check(String permission) {
        CurrentAuthUser authUser =
                Optional.ofNullable(AuthUserContext.current())
                        .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_LOGIN_MESSAGE));
        if (!authUser.hasPermission(permission)) {
            throw new BusinessException(AuthErrorCode.NO_PERMISSION);
        }
    }

}
