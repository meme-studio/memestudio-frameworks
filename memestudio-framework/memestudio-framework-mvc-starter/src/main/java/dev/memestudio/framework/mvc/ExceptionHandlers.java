package dev.memestudio.framework.mvc;

import dev.memestudio.framework.common.error.BusinessException;
import dev.memestudio.framework.common.error.ErrorMessage;
import dev.memestudio.framework.common.error.ParamErrorCode;
import dev.memestudio.framework.common.error.SystemErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author meme
 */
@ControllerAdvice
@ResponseBody
@Slf4j
@AllArgsConstructor
public class ExceptionHandlers {

    private final String appName;

    /**
     * 其他系统非预期异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorMessage handleException(Exception ex) {
        int hashCode = ex.hashCode();
        log.error("发生系统异常, hash值为[{}]", hashCode);
        log.error(ex.getMessage(), ex);
        return ErrorMessage.of(SystemErrorCode.of(hashCode, ex.getMessage()), appName);
    }

    /**
     * 业务异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(BusinessException.class)
    public ErrorMessage handleBusinessException(BusinessException ex) {
        log.warn(ex.getMessage());
        return ErrorMessage.of(ex, appName);
    }

    /**
     * 参数异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            ServletRequestBindingException.class,
            MethodArgumentNotValidException.class,
            TypeMismatchException.class
    })
    public ErrorMessage handleParamException(Exception ex) {
        return ErrorMessage.of(ParamErrorCode.of(ex.hashCode(), ex.getMessage()), appName);
    }



}
