//package dev.memestudio.framework.security.auth.server;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dev.memestudio.framework.redis.RedisOps;
//import dev.memestudio.framework.security.context.AuthConstants;
//import dev.memestudio.framework.security.context.CurrentAuthUser;
//import dev.memestudio.framework.security.context.UserIdService;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.io.buffer.DataBuffer;
//import org.springframework.core.io.buffer.DataBufferUtils;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicReference;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class AuthGlobalFilter implements GlobalFilter {
//
//    private final ObjectMapper objectMapper;
//
//    private final UserIdService userIdService;
//
//    private final RedisOps redisOps;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        return chain.filter(exchange)
//                    .then(response(exchange));
//    }
//
//    private Mono<Void> response(ServerWebExchange exchange) {
//        AtomicReference<Map<String, String>> authRequest = new AtomicReference<>();
//        exchange.getRequest()
//                .getBody()
//                .map(this::getAuthRequest)
//                .subscribe(authRequest::set);
//
//        CurrentAuthUser authUser =
//                userIdService.get(
//                        authRequest.get().get("username"),
//                        authRequest.get().get("password"));
//        return Optional.ofNullable(authUser)
//                       .map(this::generateToken)
//                       .map(authToken -> response(authToken, exchange))
//                       .orElseThrow(() -> new IllegalArgumentException("fdjalfjfdls"));
//    }
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
//    private AuthToken generateToken(CurrentAuthUser currentAuthUser) {
//        String token = UUID.randomUUID().toString();
//        String refreshToken = UUID.randomUUID().toString();
//        redisOps.hSet(AuthConstants.AUTH_TOKEN_STORE_TOKEN_KEY, token, currentAuthUser);
//        redisOps.hSet(AuthConstants.AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY, refreshToken, currentAuthUser);
//        return new AuthToken(token, refreshToken);
//    }
//
//    @SuppressWarnings("unchecked")
//    @SneakyThrows
//    private Map<String, String> getAuthRequest(DataBuffer buffer) {
//        return objectMapper.readValue(buffer.asInputStream(), Map.class);
//    }
//}