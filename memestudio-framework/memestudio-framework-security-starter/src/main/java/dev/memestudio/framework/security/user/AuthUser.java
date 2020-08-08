package dev.memestudio.framework.security.user;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Set;

/**
 * 登陆鉴权用户
 *
 * @author meme
 * @since 2020/7/31
 */
@Data
public class AuthUser implements UserDetails {

    private static final long serialVersionUID = -7987374696722369044L;

    private final String userId;

    private final String username;

    private final String password;

    private Set<GrantedAuthority> authorities = Collections.emptySet();
    private final boolean accountNonExpired = true;
    private final boolean accountNonLocked = true;
    private final boolean credentialsNonExpired = true;
    private final boolean enabled = true;

}
