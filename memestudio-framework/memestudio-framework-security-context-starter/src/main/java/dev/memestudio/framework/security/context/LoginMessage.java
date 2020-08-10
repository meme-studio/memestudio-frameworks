package dev.memestudio.framework.security.context;

import lombok.Data;

/**
 * @author meme
 * @since 2020/8/10
 */
@Data
public class LoginMessage {

    private String account;

    private String password;

}
