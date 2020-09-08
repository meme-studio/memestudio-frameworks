package dev.memestudio.framework.security.context;

import java.util.Map;
import java.util.Optional;

/**
 * @author meme
 * @since 2020/8/10
 */
public interface UserIdService {

    Optional<String> get(Map<String, Object> fetchingMessage);


}
