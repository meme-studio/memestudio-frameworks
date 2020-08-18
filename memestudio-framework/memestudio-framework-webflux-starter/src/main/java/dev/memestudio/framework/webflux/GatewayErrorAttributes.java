package dev.memestudio.framework.webflux;

import brave.Tracer;
import brave.propagation.TraceContext;
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

import static io.vavr.API.*;
import static io.vavr.Predicates.*;
import static java.util.function.Function.identity;

@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayErrorAttributes implements ErrorAttributes {

    public static final String ERROR_ATTRIBUTE = GatewayErrorAttributes.class.getName() + ".ERROR";

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
        ErrorCode errorCode =
                Match(error).of(
                        Case($(instanceOf(BusinessException.class)), this::handleBusinessException),
                        Case($(anyOf(
                                instanceOf(MethodArgumentNotValidException.class),
                                instanceOf(TypeMismatchException.class)
                        )), this::handleParamException),
                        Case($(instanceOf(RemoteException.class)), identity()),
                        Case($(instanceOf(Exception.class)), this::handleException),
                        Case($(), () -> handleUnException(determineHttpStatus(error, responseStatusAnnotation)))
                );
        TraceContext context = tracer.currentSpan()
                                     .context();
        return ErrorMessage.builder()
                           .note(errorCode.getNote())
                           .detail(errorCode.getDetail())
                           .code(errorCode.getCode())
                           .from(Option(error)
                                   .map(Throwable::getClass)
                                   .filter(RemoteException.class::isAssignableFrom)
                                   .map(__ -> (RemoteException) error)
                                   .map(RemoteException::getFrom)
                                   .getOrElse(appName))
                           .path(request.exchange().getRequest().getPath().toString())
                           .trace(context.toString())
                           .timestamp(System.currentTimeMillis())
                           .errorStacks(Arrays.asList(ExceptionUtils.getRootCauseStackTrace(error)))//TODO 暂时实现
                           .build();

    }

    /**
     * 其他系统非预期异常
     */
    private ErrorCode handleException(Exception ex) {
        int hashCode = ex.hashCode();
        log.error("发生系统异常, hash值为[{}]", hashCode);
        log.error(ex.getMessage(), ex);
        return SystemErrorCode.of(hashCode, ex.getMessage());
    }

    /**
     * 业务异常
     */
    private ErrorCode handleBusinessException(BusinessException ex) {
        log.warn(ex.getMessage());
        return ex;
    }

    /**
     * 参数异常
     */
    private ErrorCode handleParamException(Exception ex) {
        return ParamErrorCode.of(ex.hashCode(), ex.getMessage());
    }

    /**
     * 非异常情况
     */
    private ErrorCode handleUnException(HttpStatus status) {
        return Match(status).of(
                Case($(is(HttpStatus.NOT_FOUND)), () -> HttpStatusUnOkErrorCode.of("请求资源未找到")),
                Case($(), () -> HttpStatusUnOkErrorCode.of(String.format("访问失败：%d", status.value())))
        );
    }
}