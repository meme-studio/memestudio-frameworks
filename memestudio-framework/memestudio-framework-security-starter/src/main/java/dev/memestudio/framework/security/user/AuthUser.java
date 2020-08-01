package dev.memestudio.framework.security.user;

import dev.memestudio.framework.security.permission.Permission;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登陆鉴权用户
 *
 * @author meme
 * @since 2020/7/31
 */
@Data
public class AuthUser implements UserDetails {

    private final String userId;

    private final String username;

    private final String password;

    private Set<Permission> authorities;
    private final boolean accountNonExpired = true;
    private final boolean accountNonLocked = true;
    private final boolean credentialsNonExpired = true;
    private final boolean enabled = true;

    public Set<String> listPermissions() {
        return authorities.stream()
                          .map(GrantedAuthority::getAuthority)
                          .collect(Collectors.toSet());
    }

}
