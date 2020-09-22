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

    private static final String noteTemplate = "客戶端請求參數有誤，請聯系開发人員, 錯誤代碼：%s";

    private final int code = -100_01_002;

    private final String note;

    private final String detail;

    public static ErrorCode of(String trace, String detail) {
        return new ParamErrorCode(String.format(noteTemplate, trace), detail);
    }


}
