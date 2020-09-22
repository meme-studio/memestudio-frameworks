package dev.memestudio.framework.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author meme
 * @since 2020/7/3
 */
@Getter
@RequiredArgsConstructor
public class NetworkErrorCode implements ErrorCode {

    private static final String noteTemplate = "當前服務不可用，請稍后重試，错误代码：%s";

    private final int code = -100_01_003;

    private final String note;

    private final String detail;

    public static ErrorCode of(String trace, String detail) {
        return new NetworkErrorCode(String.format(noteTemplate, trace), detail);
    }


}
