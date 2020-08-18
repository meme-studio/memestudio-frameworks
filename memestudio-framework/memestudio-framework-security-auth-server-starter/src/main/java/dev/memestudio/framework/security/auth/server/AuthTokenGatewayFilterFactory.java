//package dev.memestudio.framework.security.auth.server;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dev.memestudio.framework.common.error.BusinessException;
//import dev.memestudio.framework.redis.RedisOps;
//import dev.memestudio.framework.security.context.*;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.core.io.buffer.DataBuffer;
//import org.springframework.core.io.buffer.DataBufferUtils;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.UUID;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class AuthTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
//
//    private final ObjectMapper objectMapper;
//
//    private final Map<String, UserIdService> userIdServices;
//
//    private final RedisOps redisOps;
//
//
//    @SneakyThrows
//    private Mono<Void> response(AuthToken authToken, ServerWebExchange exchange) {
//        String result = objectMapper.writeValueAsString(authToken);
//        ServerHttpResponse response = exchange.getResponse();
//        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
//        return response.writeWith(Mono.just(buffer))
//                       .doOnError(error -> DataBufferUtils.release(buffer));
//    }
//
//    private AuthToken generateToken(String userId) {
//        String token = UUID.randomUUID().toString();
//        String refreshToken = UUID.randomUUID().toString();
//        redisOps.set(AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY + ":" + token, userId);
//        redisOps.expire(AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY + ":" + token, 2 * 60 * 60 * 1000);
//        redisOps.set(AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY + ":" + refreshToken, userId);
//        redisOps.expire(AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY + ":" + refreshToken, 24 * 60 * 60 * 1000);
//        return new AuthToken(token, refreshToken);
//    }
//
//    @Override
//    public GatewayFilter apply(Object config) {
//        return (exchange, chain) -> {
//            String refreshToken = exchange.getRequest().getHeaders().getFirst("x-refresh-token");
//            if (Objects.isNull(refreshToken)) {
//                String account = exchange.getRequest().getHeaders().getFirst("x-account");
//                String password = exchange.getRequest().getHeaders().getFirst("x-password");
//                LoginMessage loginMessage = new LoginMessage();
//                loginMessage.setAccount(account);
//                loginMessage.setPassword(password);
//                String userId = managerUserIdServiceClient.get(loginMessage);
//                Optional.ofNullable(userId)
//                        .map(this::generateToken)
//                        .map(authToken -> response(authToken, exchange))
//                        .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_LOGIN_MESSAGE));
//                AuthToken authToken = generateToken(userId);
//                return response(authToken, exchange);
//            } else {
//                return Optional.ofNullable(redisOps.get(AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY + ":" + refreshToken))
//                               .map(this::generateToken)
//                               .map(authToken -> response(authToken, exchange))
//                               .orElseThrow(() -> new BusinessException(AuthErrorCode.ERROR_REFRESH_TOKEN));
//            }
//
//        };
//    }
//}
