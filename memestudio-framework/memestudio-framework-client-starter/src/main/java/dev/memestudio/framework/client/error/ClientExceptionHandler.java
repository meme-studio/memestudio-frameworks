package dev.memestudio.framework.client.error;

import dev.memestudio.framework.common.error.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author meme
 * @since 2020/8/15
 */
@Order(1)
@Slf4j
@RequiredArgsConstructor
@ControllerAdvice
@ResponseBody
public class ClientExceptionHandler {

    /**
     * 远程调用异常
     */
    @ExceptionHandler(RemoteException.class)
    public ResponseEntity<ErrorMessage> handleRemoteException(RemoteException ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus()).body(ex.getErrorMessage());
    }


}
