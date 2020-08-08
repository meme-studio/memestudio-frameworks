package dev.memestudio.framework.security.user;

import lombok.experimental.UtilityClass;

/**
 * @author meme
 * @since 2020/8/1
 */
@UtilityClass
public class AuthUserContext {

    private final ThreadLocal<CurrentAuthUser> currentAuthUser = ThreadLocal.withInitial(() -> {
        throw new IllegalStateException("当前不是登陆环境");
    });

    public CurrentAuthUser current() {
        return currentAuthUser.get();
    }

    public void setCurrent(CurrentAuthUser currentAuthUser) {
        AuthUserContext.currentAuthUser.set(currentAuthUser);
    }

    public void reset() {
        AuthUserContext.currentAuthUser.remove();
    }


}
