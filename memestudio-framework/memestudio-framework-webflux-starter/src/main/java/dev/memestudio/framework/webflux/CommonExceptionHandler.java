package dev.memestudio.framework.webflux;

import brave.Tracer;
import dev.memestudio.framework.common.error.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author meme
 * @since 2020/8/15
 */
@Slf4j
@RequiredArgsConstructor
@ControllerAdvice
@ResponseBody
public class CommonExceptionHandler {

    private final String appName;

    private final Tracer tracer;


    private ErrorMessage buildErrorMessage(ServerHttpRequest request, Throwable error, ErrorCode errorCode, String appName) {
        return ErrorMessage.builder()
                           .errorParam(Optional.of(error)
                                               .filter(BusinessException.class::isInstance)
                                               .map(BusinessException.class::cast)
                                               .map(BusinessException::getErrorParam)
                                               .orElse(null))
                           .note(errorCode.getNote())
                           .detail(errorCode.getDetail())
                           .code(errorCode.getCode())
                           .from(appName)
                           .path(request.getPath().value())
                           .trace(tracer.currentSpan().context().toString())
                           .timestamp(System.currentTimeMillis())
                           .errorStacks(Arrays.asList(ExceptionUtils.getRootCauseStackTrace(error)))//TODO 暂时实现
                           .build();
    }

    /**
     * 远程调用异常
     */
    @ExceptionHandler(RemoteException.class)
    public ResponseEntity<ErrorMessage> handleRemoteException(RemoteException ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus()).body(ex.getErrorMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleBusinessException(BusinessException ex, ServerHttpRequest request) {
        return buildErrorMessage(request, ex, ex, appName);
    }

    /**
     * 参数异常
     */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            TypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private ErrorMessage handleParamException(Exception ex, ServerHttpRequest request) {
        ErrorCode errorCode = ParamErrorCode.of(ex.hashCode(), ex.getMessage());
        return buildErrorMessage(request, ex, errorCode, appName);
    }


    /**
     * 参数异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private ErrorCode handleNotValidException(MethodArgumentNotValidException ex, ServerHttpRequest request) {
        String note = Optional.of(ex)
                              .map(MethodArgumentNotValidException::getBindingResult)
                              .map(BindingResult::getFieldError)
                              .map(error -> String.format("'%s' %s", error.getField(), error.getDefaultMessage()))
                              .orElse("");
        ErrorCode errorCode = ParamNotValidErrorCode.of(note, ex.getMessage());
        return buildErrorMessage(request, ex, errorCode, appName);
    }


    /**
     * 其他系统非预期异常
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private ErrorMessage handleException(Throwable ex, ServerHttpRequest request) {
        int hashCode = ex.hashCode();
        log.error("发生系统异常, hash值为[{}]", hashCode);
        log.error(ex.getMessage(), ex);
        ErrorCode errorCode = SystemErrorCode.of(hashCode, ex.getMessage());
        return buildErrorMessage(request, ex, errorCode, appName);
    }




}
