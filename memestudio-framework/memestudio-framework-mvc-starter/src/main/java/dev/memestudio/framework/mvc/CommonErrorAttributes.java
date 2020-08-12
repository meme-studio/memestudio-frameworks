package dev.memestudio.framework.mvc;

import brave.Tracer;
import brave.propagation.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.common.error.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

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
                        Case($(instanceOf(BusinessException.class)), this::handleBusinessException),
                        Case($(anyOf(
                                instanceOf(ServletRequestBindingException.class),
                                instanceOf(HttpMessageNotReadableException.class),
                                instanceOf(TypeMismatchException.class)
                        )), this::handleParamException),
                        Case($(instanceOf(MethodArgumentNotValidException.class)),
                                this::handleNotValidException),
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
     * 参数异常
     */
    private ErrorCode handleNotValidException(MethodArgumentNotValidException ex) {
        String note = Optional.of(ex)
                              .map(MethodArgumentNotValidException::getBindingResult)
                              .map(BindingResult::getFieldError)
                              .map(error -> String.format("'%s' %s", error.getField(), error.getDefaultMessage()))
                              .orElse("");
        return ParamNotValidErrorCode.of(note, ex.getMessage());
    }

    /**
     * 非异常情况
     */
    private ErrorCode handleUnException(int status) {
        return Match(status).of(
                Case($(is(HttpStatus.NOT_FOUND.value())), () -> HttpStatusUnOkErrorCode.of("请求资源未找到")),
                Case($(), () -> HttpStatusUnOkErrorCode.of(String.format("访问失败：%d", status)))
        );
    }
}