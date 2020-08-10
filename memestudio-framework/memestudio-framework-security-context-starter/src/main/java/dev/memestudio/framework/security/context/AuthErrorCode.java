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

    EMPTY_PERMISSIONS("auth-001", "您的授權信息已发生變更，請重新登錄", null),
    NO_PERMISSION("auth-002", "您沒有操作權限", null),
    NEED_LOGIN("auth-003", "需要登陆", null);

    private final String code;

    private final String note;

    private final String detail;

}
