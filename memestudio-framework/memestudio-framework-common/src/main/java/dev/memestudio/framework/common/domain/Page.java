package dev.memestudio.framework.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author meme
 * @since 2020/7/20
 */
@Data
public class Page<O> implements Serializable {

    private static final long serialVersionUID = -1040008233987199281L;

    private List<O> result;

    private final long total;

}
