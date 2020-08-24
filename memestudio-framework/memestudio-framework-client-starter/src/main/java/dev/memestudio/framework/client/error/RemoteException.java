package dev.memestudio.framework.client.error;


import com.netflix.hystrix.exception.HystrixBadRequestException;
import dev.memestudio.framework.common.error.ErrorMessage;
import lombok.Getter;

/**
 * 远程调用异常
 *
 * @author meme
 */
@Getter
public class RemoteException extends HystrixBadRequestException {

    private static final long serialVersionUID = -7416262267507234617L;

    private final ErrorMessage errorMessage;

    private final int status;

    public RemoteException(ErrorMessage errorMessage, int status) {
        super(String.format("远程调用[%s]错误业务异常：%s，详细信息：%s，异常代码：[%s]",
                errorMessage.getFrom(), errorMessage.getNote(), errorMessage.getDetail(), errorMessage.getCode()));
        this.errorMessage = errorMessage;
        this.status = status;
    }
}