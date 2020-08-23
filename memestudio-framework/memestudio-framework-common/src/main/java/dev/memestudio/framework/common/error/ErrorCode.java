package dev.memestudio.framework.common.error;

/**
 * ErrorCode
 *
 * @author meme
 * @since 2019-03-06 10:35
 */
public interface ErrorCode {

    String getCode();

    String getNote();

    default String getDetail() {
        return null;
    }

}
