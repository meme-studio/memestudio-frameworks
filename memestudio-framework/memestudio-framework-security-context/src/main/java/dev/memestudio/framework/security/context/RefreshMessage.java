package dev.memestudio.framework.security.context;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author meme
 * @since 2020/8/23
 */
@Data
@ApiModel("刷新token信息")
public class RefreshMessage {

    private String refreshToken;

    private String scope;

}
