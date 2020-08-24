package dev.memestudio.framework.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author meme
 * @since 2020/7/3
 */
@Getter
@RequiredArgsConstructor
public class SystemErrorCode implements ErrorCode {

    private static final String noteTemplate = "系統发生異常，請稍后重試，錯誤代碼：%s";

    private final int code = -100_01_001;

    private final String note;

    private final String detail;

    public static ErrorCode of(int exceptionHash, String detail) {
        return new SystemErrorCode(String.format(noteTemplate, exceptionHash), detail);
    }


}
