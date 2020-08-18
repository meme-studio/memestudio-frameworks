package dev.memestudio.framework.security.auth.server;

import dev.memestudio.framework.redis.RedisOps;
import dev.memestudio.framework.security.context.AuthConstants;
import dev.memestudio.framework.security.context.AuthErrorCode;
import dev.memestudio.framework.security.context.AuthException;
import dev.memestudio.framework.security.context.AuthToken;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

/**
 * @author meme
 * @since 2020/8/11
 */
@RequiredArgsConstructor
public class AuthTokenStore {

    private final RedisOps redisOps;

    private final String prefix;

    private final long tokenTimeout;

    private final long refreshTokenTimeout;

    public AuthToken generate(String userId) {
        String token = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        redisOps.setEx(String.join(":", AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY, prefix, token), userId, tokenTimeout);
        redisOps.setEx(String.join(":", AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY, prefix, refreshToken), userId, refreshTokenTimeout);
        AuthToken authToken = new AuthToken(token, refreshToken, tokenTimeout);
        redisOps.hSet(AuthConstants.AUTH_TOKEN_STORE_USERS, userId, authToken);
        return authToken;
    }

    public AuthToken fetchOrGenerate(String userId) {
        return redisOps.hGet(AuthConstants.AUTH_TOKEN_STORE_USERS, userId, AuthToken.class)
                       .map(Option::of)
                       .orElseGet(Option::none)
                       .peek(token -> token.setExpiresIn(redisOps.ttl(String.join(":", AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY, prefix, token.getToken()))))
                       .filter(token -> token.getExpiresIn() > 60)
                       .getOrElse(() -> generate(userId));
    }

    public AuthToken fetchOrGenerateByRefreshToken(String refreshToken) {
        String userId = redisOps.get(String.join(":", AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY, prefix, refreshToken));
        Optional.of(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.ERROR_REFRESH_TOKEN));
        expireToken(userId);
        return generate(userId);
    }

    public boolean hasToken(String token) {
        return redisOps.exists(String.join(":", AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY, prefix, token));
    }

    public boolean hasRefreshToken(String refreshToken) {
        return redisOps.exists(String.join(":", AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY, prefix, refreshToken));
    }

    public void expireToken(String userId) {
        redisOps.hGet(AuthConstants.AUTH_TOKEN_STORE_USERS, userId, AuthToken.class)
                .ifPresent(token -> {
                    redisOps.hDel(AuthConstants.AUTH_TOKEN_STORE_USERS, userId);
                    redisOps.expire(AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY + ":" + prefix + ":" + token.getToken(), 0);
                    redisOps.expire(AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY + ":" + prefix + ":" +  token.getRefreshToken(), 0);
                });
    }

}
