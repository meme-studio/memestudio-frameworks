package dev.memestudio.framework.security.auth.server;

import dev.memestudio.framework.security.context.AuthConstants;
import dev.memestudio.framework.security.context.AuthErrorCode;
import dev.memestudio.framework.security.context.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class TokenToUserIdFilter implements GlobalFilter {

    private final AuthTokenStore authTokenStore;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String token = headers.getFirst(AuthConstants.TOKEN_HEADER);
        String scope = headers.getFirst(AuthConstants.SCOPE_HEADER);
        ServerHttpRequest request = Optional.ofNullable(token)
                                            .map(__ -> Optional.ofNullable(authTokenStore.getUserId(token, scope))
                                                               .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_TOKEN)))
                                            .map(userId -> exchange.getRequest()
                                                                   .mutate()
                                                                   .header(AuthConstants.AUTH_USER_HEADER, userId)
                                                                   .build())
                                            .orElseGet(() -> getNoAuthHeaderRequest(exchange));
        return chain.filter(exchange.mutate()
                                    .request(request)
                                    .build());
    }

    private ServerHttpRequest getNoAuthHeaderRequest(ServerWebExchange exchange) {
        return exchange.getRequest()
                       .mutate()
                       .headers(headers -> headers.putAll(exchange.getRequest()
                                                                  .getHeaders()
                                                                  .entrySet()
                                                                  .stream()
                                                                  .filter(header -> Objects.equals(header.getKey(), AuthConstants.AUTH_USER_HEADER))
                                                                  .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))))
                       .build();
    }
}