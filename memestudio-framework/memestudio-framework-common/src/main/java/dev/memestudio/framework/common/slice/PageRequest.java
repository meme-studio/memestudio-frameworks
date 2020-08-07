package dev.memestudio.framework.common.slice;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 *
 * @author meme
 * @since 2020/8/7
 */
@NoArgsConstructor
@Data
@Valid
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1188023969742232982L;

    @Min(1)
    protected int pageNo = 1;

    protected int pageSize = 20;

    public int limit(){
        return pageSize;
    }

    public int offset(){
        return (pageNo - 1) * pageSize;
    }

}
