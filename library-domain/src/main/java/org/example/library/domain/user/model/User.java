package org.example.library.domain.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 用户聚合根：账号、角色与状态的唯一入口。
 * 禁用只影响登录与新的借阅，不改变历史记录。
 */
@Getter
@Builder
@AllArgsConstructor
public class User {

    private final Long id;

    private final UserName userName;

    private final Password password;

    private final Role role;

    private UserStatus status;

    /** 静态工厂：创建 READER 用户（注册默认角色与状态）。 */
    public static User createReader(UserName userName, Password password) {
        return User.builder()
                .userName(userName)
                .password(password)
                .role(Role.READER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public void enable() {
        this.status = UserStatus.ACTIVE;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
