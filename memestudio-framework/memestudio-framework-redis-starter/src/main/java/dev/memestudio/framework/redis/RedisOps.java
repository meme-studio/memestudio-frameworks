package dev.memestudio.framework.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static java.util.stream.Collectors.*;

/**
 * 通用redis操作类，提供范型操作
 *
 * @author meme
 * @since 2019-03-07 15:28
 */
@AllArgsConstructor
public class RedisOps {

    private final StringRedisTemplate template;

    private final ObjectMapper mapper;

    private final String scope;

    //~ Strings

    public <T> Optional<T> get(String key, Class<T> type) {
        return toType(get(key), type);
    }

    public String get(String key) {
        return opsForValue().get(toScopeKey(key));
    }

    public <T> Optional<T> getRange(String key, long start, long end, Class<T> type) {
        return toType(getRange(key, start, end), type);
    }

    public String getRange(String key, long start, long end) {
        return opsForValue().get(toScopeKey(key), start, end);
    }

    public <T> Optional<T> getSet(String key, T value, Class<T> type) {
        return toType(getSet(key, valueToString(value)), type);
    }

    public String getSet(String key, String value) {
        return opsForValue().getAndSet(toScopeKey(key), value);
    }

    public <T> void set(String key, T value) {
        opsForValue().set(toScopeKey(key), valueToString(value));
    }

    public <T> Boolean setExNx(String key, T value, long timeout) {
        return opsForValue().setIfAbsent(toScopeKey(key), valueToString(value), timeout, TimeUnit.SECONDS);
    }

    public <T> void setRange(String key, T value, long offset) {
        opsForValue().set(toScopeKey(key), valueToString(value), offset);
    }

    public <T> void setEx(String key, T value, long timeout) {
        opsForValue().set(toScopeKey(key), valueToString(value), timeout, TimeUnit.SECONDS);
    }

    public <T> Boolean setNx(String key, T value) {
        return opsForValue().setIfAbsent(toScopeKey(key), valueToString(value));
    }

    public <T> List<T> mGet(Class<T> type, String... keys) {
        return toType(mGet(keys), type);
    }

    public List<String> mGet(String... keys) {
        return opsForValue().multiGet(Arrays.stream(keys)
                                            .map(this::toScopeKey)
                                            .collect(toList()));
    }

    public <T> void mSet(Map<String, T> map) {
        opsForValue().multiSet(map.entrySet()
                                  .stream()
                                  .collect(toMap(unchecked(this::toScopeKey).compose(Map.Entry::getKey),
                                          unchecked(this::valueToString).compose(Map.Entry::getValue))));
    }

    public Long incr(String key) {
        return incrBy(key, 1);
    }

    public Long incrBy(String key, long amount) {
        return opsForValue().increment(toScopeKey(key), amount);
    }

    public Long decr(String key) {
        return decrBy(key, 1);
    }

    public Long decrBy(String key, long amount) {
        return incrBy(key, -amount);
    }


    //~ Lists

    public <T> Optional<T> lPop(String key, Class<T> type) {
        return toType(lPop(key), type);
    }

    public String lPop(String key) {
        return opsForList().leftPop(toScopeKey(key));
    }

    public <T> Optional<T> rPop(String key, Class<T> type) {
        return toType(rPop(key), type);
    }

    public String rPop(String key) {
        return opsForList().rightPop(toScopeKey(key));
    }

    @SafeVarargs
    public final <T> Long lPush(String key, T... values) {
        return opsForList().leftPushAll(toScopeKey(key), valuesToStrings(values));
    }

    public final List<String> lRange(String key, long start, long end) {
        return opsForList().range(toScopeKey(key), start, end);
    }

    public final <T> List<T> lRange(String key, long start, long end, Class<T> type) {
        return toType(lRange(key, start, end), type);
    }

    @SafeVarargs
    public final <T> Long rPush(String key, T... values) {
        return opsForList().rightPushAll(toScopeKey(key), valuesToStrings(values));
    }

    public Long lLen(String key) {
        return opsForList().size(toScopeKey(key));
    }


    //~ Hashes

    public Long hDel(String key, String... fields) {
        return opsForHash().delete(toScopeKey(key), Arrays.stream(fields)
                                                          .toArray());
    }

    public <T> Optional<T> hGet(String key, String field, Class<T> type) {
        return toType(hGet(key, field), type);
    }

    public String hGet(String key, String field) {
        return opsForHash().get(toScopeKey(key), field);
    }

    public <T> List<T> hMGet(String key, Class<T> type, String... fields) {
        return toType(hMGet(key, fields), type);
    }

    public List<String> hMGet(String key, String... fields) {
        return opsForHash().multiGet(toScopeKey(key), Arrays.asList(fields));
    }

    public <T> void hSet(String key, String field, T value) {
        opsForHash().put(toScopeKey(key), field, valueToString(value));
    }

    public <T> void hMSet(String key, Map<String, T> fieldValues) {
        opsForHash().putAll(toScopeKey(key), fieldValues.entrySet()
                                                        .stream()
                                                        .collect(toMap(Map.Entry::getKey,
                                                                unchecked(this::valueToString).compose(Map.Entry::getValue))));
    }

    public Long hLen(String key) {
        return opsForHash().size(toScopeKey(key));
    }

    public Boolean hExists(String key, String field) {
        return opsForHash().hasKey(toScopeKey(key), field);
    }

    public <T> Map<String, T> hGetAll(String key, Class<T> type) {
        return hGetAll(key).entrySet()
                           .stream()
                           .collect(toMap(Map.Entry::getKey, castType(type).compose(Map.Entry::getValue)));
    }

    public Map<String, String> hGetAll(String key) {
        return opsForHash().entries(toScopeKey(key));
    }

    public Set<String> hKeys(String pattern) {
        return opsForHash().keys(toScopeKey(pattern));
    }

    //~ Sets

    @SafeVarargs
    public final <T> Long sAdd(String key, T... members) {
        return opsForSet().add(toScopeKey(key), valuesToStrings(members));
    }

    public Long sCard(String key) {
        return opsForSet().size(toScopeKey(key));
    }

    public String sPop(String key) {
        return opsForSet().pop(toScopeKey(key));
    }

    public <T> Optional<T> sPop(String key, Class<T> type) {
        return toType(sPop(key), type);
    }

    public <T> Boolean sIsMember(String key, T member) {
        return opsForSet().isMember(toScopeKey(key), valueToString(member));
    }

    public Set<String> sMembers(String key) {
        return opsForSet().members(toScopeKey(key));
    }

    public <T> Set<T> sMembers(String key, Class<T> type) {
        return toType(sMembers(key), type);
    }

    @SafeVarargs
    public final <T> Long sRem(String key, T... members) {
        return opsForSet().remove(key, (Object[]) valuesToStrings(members));
    }

    //~ Sorted Sets

    public <T> Boolean zAdd(String key, T value, double score) {
        return opsForZSet().add(toScopeKey(key), valueToString(value), score);
    }

    public Long zCount(String key, double min, double max) {
        return opsForZSet().count(toScopeKey(key), min, max);
    }

    public <T> Double zScore(String key, T member) {
        return opsForZSet().score(toScopeKey(key), member);
    }

    @SafeVarargs
    public final <T> Long zRem(String key, T... members) {
        return opsForZSet().remove(toScopeKey(key), Arrays.stream(valuesToStrings(members))
                                                          .toArray());
    }

    public <T> Long zRank(String key, T member) {
        return opsForZSet().rank(toScopeKey(key), valueToString(member));
    }

    public <T> Long zRevRank(String key, T member) {
        return opsForZSet().reverseRank(toScopeKey(key), valueToString(member));
    }

    public Long zCard(String key) {
        return opsForZSet().zCard(toScopeKey(key));
    }

    public Set<String> zRange(String key, long start, long end) {
        return opsForZSet().range(toScopeKey(key), start, end);
    }

    public <T> Set<T> zRange(String key, long start, long end, Class<T> type) {
        return toType(zRange(key, start, end), type);
    }

    public Set<String> zRangeByScore(String key, double min, double max) {
        return opsForZSet().rangeByScore(toScopeKey(key), min, max);
    }

    public <T> Set<T> zRangeByScore(String key, double min, double max, Class<T> type) {
        return toType(zRangeByScore(key, min, max), type);
    }

    public Set<String> zRevRangeByScore(String key, double min, double max) {
        return opsForZSet().reverseRangeByScore(toScopeKey(key), min, max);
    }

    public <T> Set<T> zRevRangeByScore(String key, double min, double max, Class<T> type) {
        return toType(zRevRangeByScore(key, min, max), type);
    }


    //~ Commons

    public void del(String... keys) {
        template.delete(Arrays.stream(keys)
                              .map(this::toScopeKey)
                              .collect(toList()));
    }

    public Boolean expire(String key, long timeout) {
        return template.expire(toScopeKey(key), timeout, TimeUnit.SECONDS);
    }

    public long expire(long timeout, String... keys) {
        return Arrays.stream(keys)
                     .map(key -> expire(key, timeout))
                     .filter(Boolean::booleanValue)
                     .count();
    }

    public Boolean exists(String key) {
        return template.hasKey(toScopeKey(key));
    }

    public Set<String> keys(String pattern) {
        return template.keys(toScopeKey(pattern));
    }

    public Long ttl(String key) {
        return template.getExpire(toScopeKey(key), TimeUnit.SECONDS);
    }




    private String toScopeKey(String key) {
        return String.format("%s:%s", scope, key);
    }

    private <T> Function<String, T> castType(Class<T> type) {
        return unchecked(value -> mapper.readValue(value, type));
    }

    private <T> Optional<T> toType(String value, Class<T> type) {
        return Optional.ofNullable(value)
                       .map(castType(type));
    }

    private <T, C extends Collection<T>> C toType(Collection<String> values, Class<T> type) {
        return (C) Optional.ofNullable(values)
                       .map(__ -> values.stream()
                                        .filter(Objects::nonNull)
                                        .map(castType(type))
                                        .collect(toCollection(unchecked(() ->
                                                Match((Object) values).of(
                                                        Case($(instanceOf(Set.class)), (Supplier<HashSet<T>>) HashSet::new),
                                                        Case($(instanceOf(List.class)), (Supplier<ArrayList<T>>) ArrayList::new))))))
                       .orElse(null);
    }


    private <T> String valueToString(T value) {
        return Option.of(value)
                     .filter(String.class::isInstance)
                     .map(String.class::cast)
                     .getOrElseTry(() -> mapper.writeValueAsString(value));
    }

    private <T> String[] valuesToStrings(T[] values) {
        return Arrays.stream(values)
                     .map(this::valueToString)
                     .toArray(String[]::new);
    }

    private ValueOperations<String, String> opsForValue() {
        return template.opsForValue();
    }

    private ListOperations<String, String> opsForList() {
        return template.opsForList();
    }

    private HashOperations<String, String, String> opsForHash() {
        return template.opsForHash();
    }

    private SetOperations<String, String> opsForSet() {
        return template.opsForSet();
    }

    private ZSetOperations<String, String> opsForZSet() {
        return template.opsForZSet();
    }


}
