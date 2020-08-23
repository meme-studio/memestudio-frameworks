package dev.memestudio.framework.common.error;


import lombok.Getter;

/**
 * 业务异常，接受业务自定义的错误类型实体，错误类型实体必须实现@{@link ErrorCode}，可参照@{@link SystemErrorCode}实现自己的错误类型枚举
 *
 * @author meme
 */
@Getter
public class BusinessException extends RuntimeException implements ErrorCode {

    private static final long serialVersionUID = 2329095321329910557L;

    private final String code;

    private final String note;

    private final Object errorParam;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getNote(), null);
    }

    public BusinessException(ErrorCode errorCode, Object errorParam, Object... noteParams) {
        this(errorCode.getCode(), String.format(errorCode.getNote(), noteParams), errorParam);
    }

    private BusinessException(String code, String note, Object errorParam) {
        super(String.format("业务异常：%s，异常代码：[%s]", note, code));
        this.code = code;
        this.note = note;
        this.errorParam = errorParam;
    }



}