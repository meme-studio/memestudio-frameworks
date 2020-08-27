package dev.memestudio.framework.common.slice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "滑动分页结果")
public class ScrollingResponse<O> implements Serializable {

    private static final long serialVersionUID = 2915519051260135202L;

    @ApiModelProperty("分页结果集")
    private List<O> result;

    @ApiModelProperty("下次请求的偏移量")
    private String nextOffset;

    @ApiModelProperty("是否有下一页")
    private boolean next;

}
