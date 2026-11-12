package org.example.library.web.vo;

import lombok.Value;

import org.example.library.domain.user.model.Role;
import org.example.library.domain.user.model.User;
import org.example.library.domain.user.model.UserStatus;

/** 用户响应对象（不暴露密码哈希）。 */
@Value
public class UserVO {

    Long id;

    String userName;

    Role role;

    UserStatus status;

    public static UserVO from(User u) {
        return new UserVO(u.getId(), u.getUserName().getValue(), u.getRole(), u.getStatus());
    }
}
