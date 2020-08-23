package dev.memestudio.framework.security.context;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author meme
 * @since 2020/8/10
 */
@ApiModel("登陆信息")
@Data
public class LoginMessage {

    @ApiModelProperty("登陆账号")
    private String account;

    @ApiModelProperty("登陆密码/验证码")
    private String password;

    @ApiModelProperty("指定的客户端")
    private String scope;

}
