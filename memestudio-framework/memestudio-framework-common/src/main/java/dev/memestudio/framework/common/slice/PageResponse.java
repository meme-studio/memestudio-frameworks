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
@ApiModel(description = "分页结果")
public class PageResponse<O> implements Serializable {

    private static final long serialVersionUID = -1040008233987199281L;

    @ApiModelProperty("分页结果集")
    private List<O> result;

    @ApiModelProperty("总记录数")
    private long total;

}
