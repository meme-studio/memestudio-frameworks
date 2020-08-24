package dev.memestudio.framework.security.context;

import dev.memestudio.framework.common.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用错误类型
 *
 * @author meme
 * @since 2019-03-06 10:37
 */
@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    EMPTY_PERMISSIONS(-100_01_001, "您的授權信息已发生變更，請重新登錄"),
    NO_PERMISSION(-100_01_002, "您沒有操作權限"),
    NEED_LOGIN(-100_01_003, "需要登陆"),
    INVALID_LOGIN_MESSAGE(-100_01_004, "登陸信息有誤"),
    ERROR_REFRESH_TOKEN(-100_01_005, "登陸刷新碼錯誤"),
    NO_RESOURCE_ACCESS(-100_01_006, "您沒有當前商戶或門店的操作權限"),
    INVALID_TOKEN(-100_01_007, "授權碼錯誤"),
    INVALID_SCOPE(-100_01_008, "scope信息有误"),
    ;

    private final int code;

    private final String note;

}
