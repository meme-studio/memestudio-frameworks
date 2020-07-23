package dev.memestudio.framework.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author meme
 * @since 2020/7/20
 */
@Data
public class Scrolling<O> implements Serializable {

    private static final long serialVersionUID = 2915519051260135202L;

    private List<O> result;

    private String index;

    private boolean next;

    public Long indexAsLong() {
        return new Long(index);
    }
}
