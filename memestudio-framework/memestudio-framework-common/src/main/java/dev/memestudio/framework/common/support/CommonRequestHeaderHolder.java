package dev.memestudio.framework.common.support;

import java.util.HashMap;
import java.util.Map;

/**
 * @author meme
 * @since 2021/2/19
 */
public final class CommonRequestHeaderHolder {

    private static final ThreadLocal<Map<String, String>> context = InheritableThreadLocal.withInitial(HashMap::new);

    public static String get(String key) {
        return context.get().get(key);
    }

    public static String getClientVersion() {
        return context.get().get("x-client-version");
    }


    public static String getClientModel() {
        return context.get().get("x-client-model");
    }


    public static String getClientOs() {
        return context.get().get("x-client-os");
    }

    public static String getH5ClientOs() {
        return context.get().get("x-h5-client-os");
    }

    public static long getClientTimestamp() {
        return Long.parseLong(context.get().get("x-timestamp"));
    }

    public static void set(Map<String, String> headers) {
        context.set(headers);
    }

    public static void clear() {
        context.remove();
    }

}
