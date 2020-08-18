package dev.memestudio.framework.security.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author meme
 * @since 2020/8/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {

    private String token;

    private String refreshToken;

    private long expiresIn;

}
