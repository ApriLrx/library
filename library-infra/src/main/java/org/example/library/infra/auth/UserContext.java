package org.example.library.infra.auth;

import org.example.library.domain.user.model.Role;

/**
 * 当前请求用户上下文：由鉴权拦截器写入，业务侧只从这里取用户身份，不接受前端传入 userId。
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private static final ThreadLocal<Role> ROLE = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, Role role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Role getRole() {
        return ROLE.get();
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
