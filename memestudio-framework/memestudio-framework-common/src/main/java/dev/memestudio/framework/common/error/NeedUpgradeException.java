package dev.memestudio.framework.common.error;

import lombok.Getter;

/**
 * @author meme
 * @since 2021/3/5
 */
@Getter
public class NeedUpgradeException extends RuntimeException implements ErrorCode {

    private static final long serialVersionUID = -2041730267738456724L;

    private final int code;

    private static final int page = -100_00_007;

    private static final int notification = -100_00_008;

    private final String note = "版本過舊，當前頁面無法使用\n" +
            "請更新到最新版本";

    private NeedUpgradeException(int code) {
        this.code = code;
    }

    public static NeedUpgradeException page() {
        return new NeedUpgradeException(page);
    }

    public static NeedUpgradeException notification() {
        return new NeedUpgradeException(notification);
    }
}
