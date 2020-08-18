package dev.memestudio.framework.mvc;

import brave.Tracer;
import brave.propagation.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.Predicates.is;

@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonErrorAttributes implements ErrorAttributes, HandlerExceptionResolver {

    private static final String ERROR_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

    private final ObjectMapper objectMapper;

    private final String appName;

    private final Tracer tracer;

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
                        Case($(instanceOf(Exception.class)), this::handleException),
                        Case($(), () -> handleUnException(getAttribute(webRequest, RequestDispatcher.ERROR_STATUS_CODE)))
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
                           .path(getAttribute(webRequest, RequestDispatcher.ERROR_REQUEST_URI))
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
     * 非异常情况
     */
    private ErrorCode handleUnException(int status) {
        return Match(status).of(
                Case($(is(HttpStatus.NOT_FOUND.value())), () -> HttpStatusUnOkErrorCode.of("請求資源未找到")),
                Case($(is(HttpStatus.SERVICE_UNAVAILABLE.value())), () -> HttpStatusUnOkErrorCode.of("當前服務不可用，請稍后重試")),
                Case($(), () -> HttpStatusUnOkErrorCode.of(String.format("访问失败：%d", status)))
        );
    }
}