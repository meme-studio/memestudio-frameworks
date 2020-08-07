package dev.memestudio.framework.common.slice;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author meme
 * @since 2020/7/20
 */
@NoArgsConstructor
@Data
public class ScrollingRequest implements Serializable {

    private static final long serialVersionUID = -1841012055032211057L;

    protected String offset = "0";

    protected long size = 20;

    public long offsetAsLong() {
        return Long.parseLong(offset);
    }

}
