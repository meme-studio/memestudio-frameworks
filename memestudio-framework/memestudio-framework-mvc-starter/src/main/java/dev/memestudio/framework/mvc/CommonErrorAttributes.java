package dev.memestudio.framework.mvc;

import brave.Tracer;
import brave.propagation.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.client.ClientException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import dev.memestudio.framework.common.error.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Predicates.*;

@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonErrorAttributes implements ErrorAttributes, HandlerExceptionResolver {

    private static final String ERROR_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

    private final ObjectMapper objectMapper;

    private final String appName;

    private final Tracer tracer;

    private final boolean includeErrorStacks;

    @Override
    public Throwable getError(WebRequest webRequest) {
        Throwable exception = getAttribute(webRequest, ERROR_ATTRIBUTE);
        return (exception != null) ? exception : getAttribute(webRequest, RequestDispatcher.ERROR_EXCEPTION);
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
        return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }


    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {
        storeErrorAttributes(request, ex);
        return null;
    }

    private void storeErrorAttributes(HttpServletRequest request, Exception ex) {
        request.setAttribute(ERROR_ATTRIBUTE, ex);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        ErrorMessage errorMessage = determineErrorMessage(webRequest);
        return objectMapper.convertValue(errorMessage, Map.class);
    }

    private ErrorMessage determineErrorMessage(WebRequest webRequest) {
        Throwable error = getError(webRequest);
        ErrorCode errorCode =
                Match(error).of(
                        Case($(instanceOf(HystrixRuntimeException.class)), this::handleHystrixRuntimeException),
                        Case($(anyOf(
                                instanceOf(ConnectException.class),
                                instanceOf(TimeoutException.class)
                        )), this::handleNetworkException),
                        Case($(instanceOf(ResponseStatusException.class)), this::handleResponseStatusException),
                        Case($(instanceOf(Exception.class)), this::handleException),
                        Case($(), () -> handleUnException(getAttribute(webRequest, RequestDispatcher.ERROR_STATUS_CODE)))
                );
        TraceContext context = tracer.currentSpan()
                                     .context();

        return ErrorMessage.builder()
                           .note(errorCode.getNote())
                           .detail(errorCode.getDetail())
                           .code(errorCode.getCode())
                           .from(appName)
                           .path(getAttribute(webRequest, RequestDispatcher.ERROR_REQUEST_URI))
                           .trace(context.toString().replace("/", ","))
                           .timestamp(System.currentTimeMillis())
                           .errorStacks(includeErrorStacks ? Arrays.stream(ExceptionUtils.getRootCauseStackTrace(error))
                                                                   .map(stack -> stack.replaceAll("\t", "  "))
                                                                   .collect(Collectors.toList()) : null)
                           .build();

    }

    /**
     * 其他系统非预期异常
     */
    private ErrorCode handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return SystemErrorCode.of(getTrace(), ex.getMessage());
    }

    /**
     * 网络异常
     */
    private ErrorCode handleNetworkException(Exception ex) {
        log.warn(ex.getMessage(), ex);
        return NetworkErrorCode.of(getTrace(), ex.getMessage());
    }


    /**
     * 非异常情况
     */
    private ErrorCode handleResponseStatusException(ResponseStatusException ex) {
        return Match(ex.getStatus()).of(
                Case($(is(HttpStatus.NOT_FOUND)), () -> HttpStatusUnOkErrorCode.of("请求资源未找到", ex.getMessage())),
                Case($(is(HttpStatus.SERVICE_UNAVAILABLE)), () -> HttpStatusUnOkErrorCode.of("當前服務不可用，請稍后重試", null)),
                Case($(), () -> HttpStatusUnOkErrorCode.of(String.format("當前服務不可用，請稍后重試：%d", ex.getStatus().value()), ex.getMessage()))
        );
    }


    /**
     * 远程调用异常
     */
    public ErrorCode handleHystrixRuntimeException(HystrixRuntimeException ex) {
        log.warn(ex.getMessage(), ex);
        return Match(ExceptionUtils.getRootCause(ex))
                .of(
                        Case($(anyOf(
                                instanceOf(TimeoutException.class),
                                instanceOf(ConnectException.class),
                                instanceOf(ClientException.class)
                                )),
                                this::handleNetworkException),
                        Case($(), this::handleException)
                );
    }

    /**
     * 非异常情况
     */
    private ErrorCode handleUnException(int status) {
        return Match(status).of(
                Case($(is(HttpStatus.NOT_FOUND.value())), () -> HttpStatusUnOkErrorCode.of("請求資源未找到", null)),
                Case($(is(HttpStatus.SERVICE_UNAVAILABLE.value())), () -> HttpStatusUnOkErrorCode.of("當前服務不可用，請稍后重試", null)),
                Case($(), () -> HttpStatusUnOkErrorCode.of(String.format("當前服務不可用，請稍后重試：%d", status), null))
        );
    }


    private String getTrace() {
        return tracer.currentSpan()
                     .context()
                     .traceIdString();
    }
}