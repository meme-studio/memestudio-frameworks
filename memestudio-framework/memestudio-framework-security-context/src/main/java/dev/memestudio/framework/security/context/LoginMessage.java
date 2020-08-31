package dev.memestudio.framework.security.context;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author meme
 * @since 2020/8/10
 */
@ApiModel("登陆信息")
@Data
public class LoginMessage {

    @ApiModelProperty("登陆账号")
    private String account;

    @ApiModelProperty("登陆密钥")
    private String password;

    @ApiModelProperty("登陆附加信息")
    private Map<String, Object> additions;

    @ApiModelProperty("登陆类型，兼容openId等")
    private String type;

}
