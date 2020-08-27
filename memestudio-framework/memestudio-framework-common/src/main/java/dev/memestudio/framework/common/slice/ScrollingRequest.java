package dev.memestudio.framework.common.slice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author meme
 * @since 2020/7/20
 */
@ApiModel("滑动分页请求")
@NoArgsConstructor
@Data
public class ScrollingRequest implements Serializable {

    private static final long serialVersionUID = -1841012055032211057L;

    @ApiModelProperty("滑动偏移量[默认为0]")
    protected String offset = "0";

    @ApiModelProperty("滑动分页大小[默认为20]")
    protected long size = 20;

    public long offsetAsLong() {
        return Long.parseLong(offset);
    }

}
