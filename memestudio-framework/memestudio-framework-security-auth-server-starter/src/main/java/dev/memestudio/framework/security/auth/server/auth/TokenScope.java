package dev.memestudio.framework.security.auth.server.auth;

import lombok.Builder;
import lombok.Data;

/**
 * @author meme
 * @since 2020/9/8
 */
@Data
@Builder
public class TokenScope {

    private final UserIdService userIdService;

    private final String name;

    @Builder.Default
    private long tokenTimeout = 2 * 60 * 60;

    @Builder.Default
    private long refreshTokenTimeout = 14 * 24 * 60 * 60;

    @Builder.Default
    private final boolean singleClientLimited = false;

}
