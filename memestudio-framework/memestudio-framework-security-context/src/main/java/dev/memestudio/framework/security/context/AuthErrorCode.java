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

    EMPTY_PERMISSIONS("auth-001", "您的授權信息已发生變更，請重新登錄"),
    NO_PERMISSION("auth-002", "您沒有操作權限"),
    NEED_LOGIN("auth-003", "需要登陆"),
    INVALID_LOGIN_MESSAGE("auth-004", "登陆信息有误"),
    ERROR_REFRESH_TOKEN("auth-005", "登陆刷新码错误"),
    NO_RESOURCE_ACCESS("auth-006", "您没有当前商户或门店的操作权限"),
    INVALID_TOKEN("auth-007", "授权码错误"),
    ;

    private final String code;

    private final String note;

}
