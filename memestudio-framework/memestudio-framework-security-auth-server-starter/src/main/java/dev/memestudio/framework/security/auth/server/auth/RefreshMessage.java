package dev.memestudio.framework.security.auth.server.auth;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author meme
 * @since 2020/9/4
 */
@Data
public class RefreshMessage {

    @NotEmpty(message = "refreshToken不能为空")
    private String refreshToken;

}
