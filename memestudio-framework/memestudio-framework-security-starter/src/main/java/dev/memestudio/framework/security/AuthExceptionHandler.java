package dev.memestudio.framework.security;

import brave.Tracer;
import dev.memestudio.framework.common.error.ErrorCode;
import dev.memestudio.framework.common.error.ErrorMessage;
import dev.memestudio.framework.security.context.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.RequestDispatcher;
import java.util.Arrays;

/**
 * @author meme
 * @since 2020/8/15
 */
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class AuthExceptionHandler {

    private final String appName;

    private final Tracer tracer;


    private ErrorMessage buildErrorMessage(WebRequest webRequest, Throwable error, ErrorCode errorCode, String appName) {
        return ErrorMessage.builder()
                           .note(errorCode.getNote())
                           .detail(errorCode.getDetail())
                           .code(errorCode.getCode())
                           .from(appName)
                           .path(getAttribute(webRequest, RequestDispatcher.ERROR_REQUEST_URI))
                           .trace(tracer.currentSpan().context().toString())
                           .timestamp(System.currentTimeMillis())
                           .errorStacks(Arrays.asList(ExceptionUtils.getRootCauseStackTrace(error)))//TODO 暂时实现
                           .build();
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
        return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 鉴权异常
     */
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorMessage handleBusinessException(AuthException ex, WebRequest webRequest) {
        log.warn(ex.getMessage());
        return buildErrorMessage(webRequest, ex, ex, appName);
    }




}
