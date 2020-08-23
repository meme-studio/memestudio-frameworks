package dev.memestudio.framework.security.auth.server;

import dev.memestudio.framework.redis.RedisOps;
import dev.memestudio.framework.security.context.AuthConstants;
import dev.memestudio.framework.security.context.AuthErrorCode;
import dev.memestudio.framework.security.context.AuthException;
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

    private final long tokenTimeout;

    private final long refreshTokenTimeout;

    private AuthToken generate(String userId, String scope) {
        String token = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        redisOps.setEx(String.join(":", AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY, scope, token), userId, tokenTimeout);
        redisOps.setEx(String.join(":", AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY, scope, refreshToken), userId, refreshTokenTimeout);
        AuthToken authToken = new AuthToken(token, refreshToken, tokenTimeout);
        redisOps.hSet(AuthConstants.AUTH_TOKEN_STORE_USERS, userId, authToken);
        return authToken;
    }

    public AuthToken fetchOrGenerate(String userId, String scope) {
        return redisOps.hGet(AuthConstants.AUTH_TOKEN_STORE_USERS, userId, AuthToken.class)
                       .map(Option::of)
                       .orElseGet(Option::none)
                       .peek(token -> token.setExpiresIn(redisOps.ttl(String.join(":", AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY, scope, token.getToken()))))
                       .filter(token -> token.getExpiresIn() > 60)
                       .getOrElse(() -> generate(userId, scope));
    }

    public AuthToken fetchOrGenerateByRefreshToken(String refreshToken, String scope) {
        String userId = redisOps.get(String.join(":", AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY, scope, refreshToken));
        Optional.ofNullable(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.ERROR_REFRESH_TOKEN));
        expireAuthTokenByUserId(scope, userId);
        return generate(userId, scope);
    }

    public boolean hasToken(String token, String scope) {
        return redisOps.exists(String.join(":", AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY, scope, token));
    }

    public boolean hasRefreshToken(String refreshToken, String scope) {
        return redisOps.exists(String.join(":", AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY, scope, refreshToken));
    }

    public void expireToken(String token, String scope) {
        Optional.ofNullable(getUserId(token, scope))
                .ifPresent(userId -> expireAuthTokenByUserId(scope, userId));
    }

    private void expireAuthTokenByUserId(String scope, String userId) {
        redisOps.hGet(AuthConstants.AUTH_TOKEN_STORE_USERS, userId, AuthToken.class)
                .ifPresent(authToken -> {
                    redisOps.hDel(AuthConstants.AUTH_TOKEN_STORE_USERS, userId);
                    redisOps.expire(String.join(":", AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY, scope, authToken.getToken()), 0);
                    redisOps.expire(String.join(":", AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY, scope, authToken.getRefreshToken()), 0);
                });
    }

    public String getUserId(String token, String scope) {
        return redisOps.get(String.join(":", AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY, scope, token));
    }

}
