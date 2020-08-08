package dev.memestudio.framework.security.permission;

import dev.memestudio.framework.common.error.BusinessException;
import dev.memestudio.framework.security.AuthErrorCode;
import dev.memestudio.framework.security.user.AuthUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.function.Predicate;

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
        AuthUserContext.current()
                       .getPermissions()
                       .stream()
                       .filter(Predicate.isEqual(permission))
                       .findAny()
                       .orElseThrow(() -> new BusinessException(AuthErrorCode.NO_PERMISSION));
    }

}
