package dev.memestudio.framework.security.user;

import lombok.Data;

/**
 * 登陆鉴权用户
 *
 * @author meme
 * @since 2020/7/31
 */
@Data
public class AuthUserInfo {

    private final String userId;

    private final String username;

    private final String password;

}