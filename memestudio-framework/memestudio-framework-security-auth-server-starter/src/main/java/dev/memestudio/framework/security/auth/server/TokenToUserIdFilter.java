package dev.memestudio.framework.security.auth.server;

import dev.memestudio.framework.security.context.AuthConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Component
public class TokenToUserIdFilter implements GlobalFilter {

    @Autowired
    private AuthTokenStore authTokenStore;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(AuthConstants.TOKEN_HEADER);
        String scope = exchange.getRequest().getHeaders().getFirst(AuthConstants.SCOPE_HEADER);
        ServerHttpRequest request = Optional.ofNullable(token)
                                            .map(__ -> authTokenStore.getUserId(token, scope))
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