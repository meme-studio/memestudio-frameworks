package dev.memestudio.framework.security.auth.server;

import dev.memestudio.framework.security.context.LoginMessage;
import dev.memestudio.framework.security.context.UserIdService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "token管理")
@RequiredArgsConstructor
@RestController
public class AuthTokenHandler {

    private final AuthTokenStore authTokenStore;

    private final Map<String, UserIdService> userIdServices;

    @ApiOperation("获取token")
    @PostMapping("/token/_get")
    public AuthToken get(@RequestBody LoginMessage loginMessage) {
        UserIdService userIdService = userIdServices.get(loginMessage.getScope());
        String userId = userIdService.get(loginMessage);
        return authTokenStore.fetchOrGenerate(userId, loginMessage.getScope());
    }

    @ApiOperation("获取refreshToken刷新token信息")
    @PostMapping("/token/_refresh")
    public AuthToken refresh(@RequestBody RefreshMessage refreshMessage) {
        return authTokenStore.fetchOrGenerateByRefreshToken(refreshMessage.getRefreshToken(), refreshMessage.getScope());
    }

    @ApiOperation("移除token")
    @PostMapping("/token/_del")
    public void del(@RequestBody ExpirationMessage expirationMessage) {
        authTokenStore.expireToken(expirationMessage.getToken(), expirationMessage.getScope());
    }


}