package dev.memestudio.framework.security.auth.server.token;

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

    private final Map<String, UserIdService> userIdServices;

    @ApiOperation("获取token")
    @PostMapping("_get")
    public AuthToken get(@RequestBody LoginMessage loginMessage, @RequestHeader(AuthConstants.TOKEN_HEADER) String scope) {
        UserIdService userIdService = userIdServices.get(scope);
        String userId = userIdService.get(loginMessage);
        Optional.ofNullable(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_LOGIN_MESSAGE));
        return authTokenStore.fetchOrGenerate(userId, scope);
    }

    @ApiOperation("获取refreshToken刷新token信息")
    @PostMapping("_refresh")
    public AuthToken refresh(@RequestBody String refreshToken, @RequestHeader(AuthConstants.SCOPE_HEADER) String scope) {
        return authTokenStore.fetchOrGenerateByRefreshToken(refreshToken, scope);
    }

    @ApiOperation("移除token")
    @PostMapping("_del")
    public void del(@RequestHeader(AuthConstants.TOKEN_HEADER) String token,
                    @RequestHeader(AuthConstants.SCOPE_HEADER) String scope) {
        authTokenStore.expireToken(token, scope);
    }


}