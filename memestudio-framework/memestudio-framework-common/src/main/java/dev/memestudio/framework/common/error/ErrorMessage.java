package dev.memestudio.framework.common.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务间异常信息封装实体
 *
 * @author meme
 * @since 2019-03-05 17:31
 */
@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor
public class ErrorMessage implements ErrorCode {

    @NonNull
    private String code;

    @NonNull
    private String note;

    @NonNull
    private String detail;

    @NonNull
    private String from;

    private List<String> errorStacks = new ArrayList<>();

    private ErrorMessage(ErrorCode errorCode, String from, List<String> errorStacks, String errorStack) {
        this(errorCode.getCode(), errorCode.getNote(), errorCode.getDetail(), from);
        this.errorStacks.addAll(errorStacks);
        this.errorStacks.add(errorStack);
    }

    public static ErrorMessage of(ErrorCode errorCode, String from) {
        return ErrorMessage.of(errorCode.getCode(), errorCode.getNote(), errorCode.getDetail(), from);
    }
    public static ErrorMessage of(ErrorCode errorCode, String from, String errorStack) {
        return new ErrorMessage(errorCode, from, getTraceableErrorStack(from, errorStack));
    }

    private ErrorMessage(ErrorCode errorCode, String origin, String errorStack) {
        this(errorCode.getCode(), errorCode.getNote(), errorCode.getDetail(), origin);
        errorStacks.add(errorStack);
    }

    public ErrorMessage() {}

    private static String getTraceableErrorStack(String origin, String errorStack) {
        return String.format("[%s]: %s", origin, errorStack);
    }

}

