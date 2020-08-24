package dev.memestudio.framework.security.auth.server.token;

import dev.memestudio.framework.security.context.ExpirationMessage;
import dev.memestudio.framework.security.context.LoginMessage;
import dev.memestudio.framework.security.context.RefreshMessage;
import dev.memestudio.framework.security.context.UserIdService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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