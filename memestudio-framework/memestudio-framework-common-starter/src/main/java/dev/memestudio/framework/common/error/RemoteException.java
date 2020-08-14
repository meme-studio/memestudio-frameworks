package dev.memestudio.framework.common.error;


import com.netflix.hystrix.exception.HystrixBadRequestException;
import lombok.Getter;

/**
 * 远程调用异常
 *
 * @author meme
 */
@Getter
public class RemoteException extends HystrixBadRequestException implements ErrorCode {

    private static final long serialVersionUID = -7416262267507234617L;

    private final String code;

    private final String note;

    private final String detail;

    private final String from;

    public RemoteException(ErrorMessage errorMessage) {
        this(errorMessage.getCode(), errorMessage.getNote(), errorMessage.getDetail(), errorMessage.getFrom());
    }

    public RemoteException(String code, String note, String detail, String from) {
        super(String.format("远程调用[%s]错误业务异常：%s，详细信息：%s，异常代码：[%s]", from, note, detail, code));
        this.code = code;
        this.note = note;
        this.detail = detail;
        this.from = from;
    }
}