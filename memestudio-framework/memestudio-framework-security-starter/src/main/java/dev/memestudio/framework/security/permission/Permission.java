package dev.memestudio.framework.security.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author meme
 * @since 2020/8/1
 */
@Setter
@Getter
@AllArgsConstructor
public class Permission implements GrantedAuthority {

    private String authority;

}
