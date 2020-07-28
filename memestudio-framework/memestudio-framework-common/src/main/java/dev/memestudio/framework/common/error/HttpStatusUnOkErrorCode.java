package dev.memestudio.framework.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author meme
 * @since 2020/7/3
 */
@Getter
@RequiredArgsConstructor
public class HttpStatusUnOkErrorCode implements ErrorCode {

    private final String code = "000-003";

    private final String note;

    private final String detail;

    public static ErrorCode of(String desc) {
        return new HttpStatusUnOkErrorCode(desc, desc);
    }


}
