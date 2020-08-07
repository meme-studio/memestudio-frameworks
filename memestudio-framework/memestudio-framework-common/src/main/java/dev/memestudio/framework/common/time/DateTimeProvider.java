package dev.memestudio.framework.common.time;

import java.time.LocalDateTime;

/**
 * @author meme
 * @since 2020/8/7
 */
public interface DateTimeProvider {

    default LocalDateTime now() {
        return LocalDateTime.now();
    }

}
