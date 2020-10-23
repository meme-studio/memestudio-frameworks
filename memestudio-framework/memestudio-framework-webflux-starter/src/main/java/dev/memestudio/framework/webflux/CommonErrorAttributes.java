package dev.memestudio.framework.webflux;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.client.ClientException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import dev.memestudio.framework.common.error.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Predicates.*;

@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonErrorAttributes implements ErrorAttributes {

    public static final String ERROR_ATTRIBUTE = CommonErrorAttributes.class.getName() + ".ERROR";

    private final ObjectMapper objectMapper;

    private final String appName;

    private final Tracer tracer;

    private final boolean includeErrorStacks;

    @Override
    public Throwable getError(ServerRequest request) {
        return (Throwable) request.attribute(ERROR_ATTRIBUTE)
                                  .orElseThrow(() -> new IllegalStateException("Missing exception attribute in ServerWebExchange"));
    }


    @Override
    public void storeErrorInformation(Throwable error, ServerWebExchange exchange) {
        exchange.getAttributes().putIfAbsent(ERROR_ATTRIBUTE, error);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        ErrorMessage errorMessage = determineErrorMessage(request);
        return objectMapper.convertValue(errorMessage, Map.class);
    }


    private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getStatus();
        }
        return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorMessage determineErrorMessage(ServerRequest request) {
        Throwable error = getError(request);
        MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations
                .from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
        return Match(error).of(
                Case($(instanceOf(BusinessException.class)), ex -> handleBusinessException(ex, request)),
                Case($(instanceOf(HystrixRuntimeException.class)), ex -> handleHystrixRuntimeException(ex, request)),
                Case($(anyOf(
                        instanceOf(MethodArgumentNotValidException.class),
                        instanceOf(TypeMismatchException.class)
                )), ex -> handleParamException(ex, request)),
                Case($(anyOf(
                        instanceOf(ConnectException.class),
                        instanceOf(TimeoutException.class)
                )), ex -> handleNetworkException(ex, request)),
                Case($(instanceOf(ResponseStatusException.class)), ex -> handleResponseStatusException(ex, request)),
                Case($(instanceOf(Exception.class)), ex -> handleException(ex, request)),
                Case($(), () -> handleUnException(determineHttpStatus(error, responseStatusAnnotation), request))
        );
    }

    private ErrorMessage buildErrorMessage(ServerRequest request, Throwable error, ErrorCode errorCode) {
        return ErrorMessage.builder()
                           .errorParam(Optional.ofNullable(error)
                                               .filter(BusinessException.class::isInstance)
                                               .map(BusinessException.class::cast)
                                               .map(BusinessException::getErrorParam)
                                               .orElse(null))
                           .note(errorCode.getNote())
                           .detail(errorCode.getDetail())
                           .code(errorCode.getCode())
                           .from(appName)
                           .path(request.exchange().getRequest().getPath().toString())
                           .trace(tracer.currentSpan().context().toString().replace("/", ","))
                           .timestamp(System.currentTimeMillis())
                           .errorStacks(includeErrorStacks ? Arrays.stream(ExceptionUtils.getRootCauseStackTrace(error))
                                                                   .map(stack -> stack.replaceAll("\t", "  "))
                                                                   .collect(Collectors.toList()) : null)
                           .build();
    }

    /**
     * 其他系统非预期异常
     */
    private ErrorMessage handleException(Throwable ex, ServerRequest request) {
        log.error(ex.getMessage(), ex);
        ErrorCode errorCode = SystemErrorCode.of(getTrace(), ex.getMessage());
        return buildErrorMessage(request, ex, errorCode);
    }

    /**
     * 业务异常
     */
    private ErrorMessage handleBusinessException(BusinessException ex, ServerRequest request) {
        log.warn(ex.getMessage());
        return buildErrorMessage(request, ex, ex);
    }

    /**
     * 参数异常
     */
    private ErrorMessage handleParamException(Exception ex, ServerRequest request) {
        ErrorCode errorCode = ParamErrorCode.of(getTrace(), ex.getMessage());
        return buildErrorMessage(request, ex, errorCode);
    }

    /**
     * 非异常情况
     */
    private ErrorMessage handleResponseStatusException(ResponseStatusException ex, ServerRequest request) {
        log.error(ex.getMessage(), ex);
        ErrorCode errorCode = Match(ex.getStatus()).of(
                Case($(is(HttpStatus.NOT_FOUND)), () -> HttpStatusUnOkErrorCode.of("请求资源未找到", ex.getMessage())),
                Case($(is(HttpStatus.SERVICE_UNAVAILABLE)), () -> HttpStatusUnOkErrorCode.of("當前服務不可用，請稍后重試", null)),
                Case($(), () -> HttpStatusUnOkErrorCode.of(String.format("當前服務不可用，請稍后重試：%d", ex.getStatus().value()), ex.getMessage()))
        );
        return buildErrorMessage(request, ex, errorCode);
    }

    /**
     * 非异常情况
     */
    private ErrorMessage handleUnException(HttpStatus status, ServerRequest request) {
        ErrorCode errorCode = Match(status).of(
                Case($(is(HttpStatus.NOT_FOUND)), () -> HttpStatusUnOkErrorCode.of("请求资源未找到", null)),
                Case($(is(HttpStatus.SERVICE_UNAVAILABLE)), () -> HttpStatusUnOkErrorCode.of("當前服務不可用，請稍后重試", null)),
                Case($(), () -> HttpStatusUnOkErrorCode.of(String.format("當前服務不可用，請稍后重試：%d", status.value()), null))
        );
        return buildErrorMessage(request, null, errorCode);
    }

    /**
     * 网络异常
     */
    private ErrorMessage handleNetworkException(Exception ex, ServerRequest request) {
        log.warn(ex.getMessage(), ex);
        ErrorCode errorCode = NetworkErrorCode.of(getTrace(), ex.getMessage());
        return buildErrorMessage(request, ex, errorCode);
    }

    /**
     * 远程调用异常
     */
    public ErrorMessage handleHystrixRuntimeException(HystrixRuntimeException ex, ServerRequest request) {
        log.warn(ex.getMessage(), ex);
        return Match(ExceptionUtils.getRootCause(ex))
                .of(
                        Case($(anyOf(
                                instanceOf(TimeoutException.class),
                                instanceOf(ConnectException.class),
                                instanceOf(SocketTimeoutException.class),
                                instanceOf(ClientException.class)
                                )),
                                t -> handleNetworkException(t, request)),
                        Case($(), t -> handleException(t, request))
                );
    }

    private String getTrace() {
        return tracer.currentSpan()
                     .context()
                     .traceIdString();
    }
}