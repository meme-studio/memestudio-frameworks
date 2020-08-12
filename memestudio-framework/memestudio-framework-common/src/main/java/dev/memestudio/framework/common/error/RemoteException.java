package dev.memestudio.framework.common.error;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 远程调用异常
 *
 * @author meme
 */
@Getter
@AllArgsConstructor
public class RemoteException extends RuntimeException implements ErrorCode {

    private static final long serialVersionUID = -7416262267507234617L;

    private final String code;

    private final String note;

    private final String detail;

    private final String from;

    public RemoteException(ErrorMessage errorMessage) {
        this(errorMessage.getCode(), errorMessage.getNote(), errorMessage.getDetail(), errorMessage.getFrom());
    }

}