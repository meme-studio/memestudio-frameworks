package dev.memestudio.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.common.error.BusinessException;
import dev.memestudio.framework.security.context.*;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author meme
 * @since 2020/8/1
 */
@RequiredArgsConstructor
public class AuthUserInterceptor extends HandlerInterceptorAdapter {

    private final PermissionProvider permissionProvider;

    private final ResourceAccessProvider resourceAccessProvider;

    private final AccessTypeHeaderMapper accessTypeHeaderMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(AuthConstants.AUTH_USER_HEADER);
        checkLoginStatus(userId, handler);
        Map<AccessType, String> currentResourceAccess =
                accessTypeHeaderMapper.getMappings()
                                      .entrySet()
                                      .stream()
                                      .map(mapping -> new AbstractMap.SimpleEntry<>(mapping.getKey(), request.getHeader(mapping.getValue())))
                                      .filter(entry -> Objects.nonNull(entry.getValue()))
                                      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Option.of(userId)
              .peek(__ -> determineAuthUser(userId, currentResourceAccess));
        return true;
    }

    private void checkLoginStatus(String userId, Object handler) {
        Optional.ofNullable(handler)
                .filter(HandlerMethod.class::isInstance)
                .map(HandlerMethod.class::cast)
                .map(handlerMethod -> handlerMethod.getMethodAnnotation(NeedLogin.class))
                .ifPresent(__ -> Optional.ofNullable(userId)
                                         .orElseThrow(() -> new BusinessException(AuthErrorCode.NEED_LOGIN)));
    }

    @SneakyThrows
    private void determineAuthUser(String userId, Map<AccessType, String> currentResourceAccess) {
        CurrentAuthUser authUser = new CurrentAuthUser();
        authUser.setUserId(userId);
        authUser.setPermissions(permissionProvider.provide(authUser.getUserId()));
        authUser.setResourceAccess(resourceAccessProvider.provide(authUser.getUserId()));
        authUser.setCurrentResourceAccess(currentResourceAccess);
        AuthUserContext.setCurrent(authUser);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        AuthUserContext.reset();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthUserContext.reset();
    }

}
