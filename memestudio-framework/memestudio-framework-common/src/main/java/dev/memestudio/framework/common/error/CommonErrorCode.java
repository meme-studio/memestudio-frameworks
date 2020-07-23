package dev.memestudio.framework.common.error;

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
public enum CommonErrorCode implements ErrorCode {

    API_OUT_OF_DATE("CM0002", "该接口已停止使用，请升级至新版本", null),
    APP_OUT_OF_DATE("CM0003", "请将客户端升级至新版本", null);

    private final String code;

    private final String note;

    private final String detail;

}
