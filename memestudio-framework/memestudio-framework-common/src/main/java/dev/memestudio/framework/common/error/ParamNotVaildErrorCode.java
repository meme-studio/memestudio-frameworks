package dev.memestudio.framework.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author meme
 * @since 2020/7/3
 */
@Getter
@RequiredArgsConstructor
public class ParamNotVaildErrorCode implements ErrorCode {

    private static final String noteTemplate = "請求參數有誤, %s";

    private final String code = "system-003";

    private final String note;

    private final String detail;

    public static ErrorCode of(String message, String detail) {
        return new ParamNotVaildErrorCode(String.format(noteTemplate, message), detail);
    }


}
