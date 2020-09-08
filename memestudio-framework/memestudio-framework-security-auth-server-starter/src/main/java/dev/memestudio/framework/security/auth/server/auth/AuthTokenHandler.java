package dev.memestudio.framework.security.auth.server.auth;

import dev.memestudio.framework.security.context.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Api(tags = "token管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("token")
public class AuthTokenHandler {

    private final AuthTokenStore authTokenStore;

    private final Map<String, TokenScope> tokenScopes;

    @ApiOperation("获取token")
    @PostMapping("_get")
    public AuthToken get(@RequestBody Map<String, Object> loginMessage, @RequestHeader(AuthConstants.SCOPE_HEADER) String scope) {
        return Optional.ofNullable(tokenScopes.get(scope))
                       .map(tokenScope -> tokenScope.getUserIdService()
                                                    .get(loginMessage)
                                                    .map(userId -> authTokenStore.fetchOrGenerate(userId, scope, tokenScope.getTokenTimeout(), tokenScope.getRefreshTokenTimeout(), tokenScope.isSingleClientLimited()))
                                                    .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_LOGIN_MESSAGE)))
                       .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_SCOPE));
    }

    @ApiOperation("获取refreshToken刷新token信息")
    @PostMapping("_refresh")
    public AuthToken refresh(@RequestBody RefreshMessage refreshMessage, @RequestHeader(AuthConstants.SCOPE_HEADER) String scope) {
        return Optional.ofNullable(tokenScopes.get(scope))
                       .map(tokenScope -> authTokenStore.fetchOrGenerateByRefreshToken(refreshMessage.getRefreshToken(), scope, tokenScope.getTokenTimeout(), tokenScope.getRefreshTokenTimeout()))
                       .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_SCOPE));
    }

    @ApiOperation("移除token")
    @PostMapping("_clear")
    public void del(@RequestHeader(AuthConstants.TOKEN_HEADER) String token,
                    @RequestHeader(AuthConstants.SCOPE_HEADER) String scope) {
        authTokenStore.expireToken(token, scope);
    }


}