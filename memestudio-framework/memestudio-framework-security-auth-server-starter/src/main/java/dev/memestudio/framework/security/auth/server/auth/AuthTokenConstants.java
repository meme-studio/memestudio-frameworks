package dev.memestudio.framework.security.auth.server.auth;

import lombok.experimental.UtilityClass;

/**
 * @author meme
 * @since 2020/9/8
 */
@UtilityClass
public class AuthTokenConstants {

    public static final String TOKEN_HEADER = "x-token";

    public static final String SCOPE_HEADER = "x-scope";

    public static final String AUTH_TOKEN_STORE_TOKEN_KEY = "auth_token_store_token";

    public static final String AUTH_TOKEN_STORE_REFRESH_TOKEN_KEY = "auth_token_store_refresh_token";

    public static final String AUTH_TOKEN_STORE_USERS = "auth_token_store_users";
}
