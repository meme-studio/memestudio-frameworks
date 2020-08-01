package dev.memestudio.framework.security.user;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;

/**
 * @author meme
 */
@AllArgsConstructor
public class AuthUserIdMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        return new NamedValueInfo(AuthUserConstants.AUTH_USER_HEADER, false, null);
    }

    @SneakyThrows
    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) {
        String user = request.getHeader(name);
        return  objectMapper.readValue(user, AuthUser.class)
                            .getUserId();
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUserId.class);
    }

}