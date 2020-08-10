package dev.memestudio.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.security.context.AuthConstants;
import dev.memestudio.framework.security.context.AuthUserId;
import dev.memestudio.framework.security.context.CurrentAuthUser;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
        return new NamedValueInfo(AuthConstants.AUTH_USER_HEADER, false, null);
    }

    @SneakyThrows
    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) {
        return  objectMapper.readValue(request.getHeader(name), CurrentAuthUser.class)
                            .getUserId();
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUserId.class);
    }

}