package dev.memestudio.framework.security.user;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * 鉴权信息传递
 *
 * @author meme
 * @since 2019-03-12 10:02
 */
public class AuthUserClientRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(request -> request.getHeader(AuthUserConstants.AUTH_USER_HEADER))
                .ifPresent(authUser -> template.header(AuthUserConstants.AUTH_USER_HEADER, authUser));
    }

}
