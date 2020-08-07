package dev.memestudio.framework.common.slice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author meme
 * @since 2020/7/20
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScrollingResponse<O> implements Serializable {

    private static final long serialVersionUID = 2915519051260135202L;

    private List<O> result;

    private String nextOffset;

    private boolean next;

}
