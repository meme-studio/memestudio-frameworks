package dev.memestudio.framework.redis.support;

import dev.memestudio.framework.common.support.NumericIdGenerator;
import dev.memestudio.framework.redis.RedisOps;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RedisIdGenerator
 *
 * @author meme
 * @since 2019-04-03 10:11
 */
@AllArgsConstructor
public class RedisNumericIdGenerator implements NumericIdGenerator {

    private static final DateTimeFormatter ID_PREFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final RedisOps redisOps;

    private final String key;

    @Override
    public String generateId() {
        return Option.of(redisOps.incr(key))
                     .peek(id -> Option.of(id)
                                       .filter(thisId -> thisId > 9900L)
                                       .peek(__ -> redisOps.del(key)))
                     .map(id -> String.format("%s%04d", LocalDateTime.now().format(ID_PREFIX_FORMAT), id))
                     .getOrElseThrow(() -> new IllegalStateException("Generate id error!"));
    }


}
