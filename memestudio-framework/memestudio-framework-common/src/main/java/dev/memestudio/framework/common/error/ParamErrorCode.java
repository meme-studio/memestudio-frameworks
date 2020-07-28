package dev.memestudio.framework.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author meme
 * @since 2020/7/3
 */
@Getter
@RequiredArgsConstructor
public class ParamErrorCode implements ErrorCode {

    private static final String noteTemplate = "请求参数有误, 错误代码：%s";

    private final String code = "000-002";

    private final String note;

    private final String detail;

    public static ErrorCode of(int exceptionHash, String detail) {
        return new ParamErrorCode(String.format(noteTemplate, exceptionHash), detail);
    }


}
