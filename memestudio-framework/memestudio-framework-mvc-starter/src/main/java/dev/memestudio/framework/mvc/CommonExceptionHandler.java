package dev.memestudio.framework.mvc;

import brave.Tracer;
import dev.memestudio.framework.common.error.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.RequestDispatcher;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author meme
 * @since 2020/8/15
 */
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class CommonExceptionHandler {

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
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleBusinessException(BusinessException ex, WebRequest webRequest) {
        log.warn(ex.getMessage());
        return buildErrorMessage(webRequest, ex, ex, appName);
    }

    /**
     * 参数异常
     */
    @ExceptionHandler({
            ServletRequestBindingException.class,
            HttpMessageNotReadableException.class,
            TypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private ErrorMessage handleParamException(Exception ex, WebRequest webRequest) {
        ErrorCode errorCode = ParamErrorCode.of(ex.hashCode(), ex.getMessage());
        return buildErrorMessage(webRequest, ex, errorCode, appName);
    }


    /**
     * 参数异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private ErrorCode handleNotValidException(MethodArgumentNotValidException ex, WebRequest webRequest) {
        String note = Optional.of(ex)
                              .map(MethodArgumentNotValidException::getBindingResult)
                              .map(BindingResult::getFieldError)
                              .map(error -> String.format("'%s' %s", error.getField(), error.getDefaultMessage()))
                              .orElse("");
        ErrorCode errorCode = ParamNotValidErrorCode.of(note, ex.getMessage());
        return buildErrorMessage(webRequest, ex, errorCode, appName);
    }


    /**
     * 其他系统非预期异常
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private ErrorMessage handleException(Throwable ex, WebRequest webRequest) {
        int hashCode = ex.hashCode();
        log.error("发生系统异常, hash值为[{}]", hashCode);
        log.error(ex.getMessage(), ex);
        ErrorCode errorCode = SystemErrorCode.of(hashCode, ex.getMessage());
        return buildErrorMessage(webRequest, ex, errorCode, appName);
    }




}
