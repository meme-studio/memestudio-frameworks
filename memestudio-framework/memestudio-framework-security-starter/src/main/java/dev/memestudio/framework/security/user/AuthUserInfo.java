package dev.memestudio.framework.security.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登陆鉴权用户
 *
 * @author meme
 * @since 2020/7/31
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserInfo {

    private String userId;

    private String username;

    private String password;

}
