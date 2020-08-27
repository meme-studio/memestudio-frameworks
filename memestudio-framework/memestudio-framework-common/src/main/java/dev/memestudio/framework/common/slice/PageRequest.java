package dev.memestudio.framework.common.slice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "分页请求")
@NoArgsConstructor
@Data
@Valid
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1188023969742232982L;

    @ApiModelProperty("页码[默认为1]")
    @Min(1)
    protected long pageNo = 1;

    @ApiModelProperty("分页大小[默认为20]")
    protected long pageSize = 20;

    public ScrollingRequest toScrolling() {
        ScrollingRequest request = new ScrollingRequest();
        request.setOffset(String.valueOf((pageNo - 1) * pageSize));
        request.setSize(pageSize);
        return request;
    }

}
