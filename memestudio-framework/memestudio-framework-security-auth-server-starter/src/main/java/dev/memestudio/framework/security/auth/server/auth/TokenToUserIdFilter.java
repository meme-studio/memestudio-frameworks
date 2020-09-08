package dev.memestudio.framework.security.auth.server.auth;

import dev.memestudio.framework.security.context.AuthConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RequiredArgsConstructor
public class TokenToUserIdFilter implements GlobalFilter, Ordered {

    private final AuthTokenStore authTokenStore;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String token = headers.getFirst(AuthTokenConstants.TOKEN_HEADER);
        String scope = headers.getFirst(AuthTokenConstants.SCOPE_HEADER);
        ServerHttpRequest request = Optional.ofNullable(token)
                                            .map(__ -> authTokenStore.getUserId(token, scope))
                                            .map(userId -> exchange.getRequest()
                                                                   .mutate()
                                                                   .header(AuthConstants.AUTH_USER_HEADER, userId)
                                                                   .headers(httpHeaders -> {
                                                                       httpHeaders.remove(AuthTokenConstants.TOKEN_HEADER);
                                                                       httpHeaders.remove(AuthTokenConstants.SCOPE_HEADER);
                                                                   })
                                                                   .build())
                                            .orElseGet(() -> getNoAuthHeaderRequest(exchange));
        return chain.filter(exchange.mutate()
                                    .request(request)
                                    .build());
    }

    private ServerHttpRequest getNoAuthHeaderRequest(ServerWebExchange exchange) {
        return exchange.getRequest()
                       .mutate()
                       .headers(headers -> {
                           headers.remove(AuthConstants.AUTH_USER_HEADER);
                           headers.remove(AuthTokenConstants.SCOPE_HEADER);
                           headers.remove(AuthTokenConstants.TOKEN_HEADER);
                       })
                       .build();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}