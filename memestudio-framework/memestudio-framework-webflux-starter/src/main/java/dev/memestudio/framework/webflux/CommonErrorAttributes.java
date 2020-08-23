package dev.memestudio.framework.webflux;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

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
                Case($(anyOf(
                        instanceOf(MethodArgumentNotValidException.class),
                        instanceOf(TypeMismatchException.class)
                )), ex -> handleParamException(ex, request)),
                Case($(instanceOf(RemoteException.class)), RemoteException::getErrorMessage),
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
                           .trace(tracer.currentSpan().context().toString())
                           .timestamp(System.currentTimeMillis())
                           .errorStacks(Arrays.asList(ExceptionUtils.getRootCauseStackTrace(error)))//TODO 暂时实现
                           .build();
    }

    /**
     * 其他系统非预期异常
     */
    private ErrorMessage handleException(Exception ex, ServerRequest request) {
        int hashCode = ex.hashCode();
        log.error("发生系统异常, hash值为[{}]", hashCode);
        log.error(ex.getMessage(), ex);
        ErrorCode errorCode = SystemErrorCode.of(hashCode, ex.getMessage());
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
        ErrorCode errorCode = ParamErrorCode.of(ex.hashCode(), ex.getMessage());
        return buildErrorMessage(request, ex, errorCode);
    }

    /**
     * 非异常情况
     */
    private ErrorMessage handleUnException(HttpStatus status, ServerRequest request) {
        ErrorCode errorCode = Match(status).of(
                Case($(is(HttpStatus.NOT_FOUND)), () -> HttpStatusUnOkErrorCode.of("请求资源未找到")),
                Case($(), () -> HttpStatusUnOkErrorCode.of(String.format("访问失败：%d", status.value())))
        );
        return buildErrorMessage(request, null, errorCode);
    }
}