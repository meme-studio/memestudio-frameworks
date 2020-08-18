package dev.memestudio.framework.security.context;

import dev.memestudio.framework.common.error.BusinessException;
import dev.memestudio.framework.common.error.ErrorCode;

/**
 * @author meme
 * @since 2020/8/15
 */
public class AuthException extends BusinessException {

    private static final long serialVersionUID = 6045055872058980687L;

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, Object... params) {
        super(errorCode, params);
    }
}
