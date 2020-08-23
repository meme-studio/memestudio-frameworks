package dev.memestudio.framework.security.auth.server;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author meme
 * @since 2020/8/23
 */
@Data
@ApiModel("移除token信息")
public class ExpirationMessage {

    private String token;

    private String scope;

}
