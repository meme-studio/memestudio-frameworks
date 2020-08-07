package dev.memestudio.framework.common.time;

import java.util.Date;

/**
 * @author meme
 * @since 2020/8/7
 */
public interface DateProvider {

    default Date now() {
        return new Date();
    }

}
