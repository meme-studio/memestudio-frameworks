package dev.memestudio.framework.security.auth.server.token;

import dev.memestudio.framework.security.context.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@CrossOrigin
@Api(tags = "token管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("token")
public class AuthTokenHandler {

    private final AuthTokenStore authTokenStore;

    private final Map<String, UserIdService> userIdServices;

    @ApiOperation("获取token")
    @PostMapping("_get")
    public AuthToken get(@RequestBody LoginMessage loginMessage) {
        UserIdService userIdService = userIdServices.get(loginMessage.getScope());
        String userId = userIdService.get(loginMessage);
        Optional.ofNullable(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_LOGIN_MESSAGE));
        return authTokenStore.fetchOrGenerate(userId, loginMessage.getScope());
    }

    @ApiOperation("获取refreshToken刷新token信息")
    @PostMapping("_refresh")
    public AuthToken refresh(@RequestBody RefreshMessage refreshMessage) {
        return authTokenStore.fetchOrGenerateByRefreshToken(refreshMessage.getRefreshToken(), refreshMessage.getScope());
    }

    @ApiOperation("移除token")
    @PostMapping("_del")
    public void del(@RequestBody ExpirationMessage expirationMessage) {
        authTokenStore.expireToken(expirationMessage.getToken(), expirationMessage.getScope());
    }


}