package dev.memestudio.framework.security.context;

/**
 * @author meme
 * @since 2020/8/10
 */
public interface UserIdService {

    String get(LoginMessage loginMessage);

}
