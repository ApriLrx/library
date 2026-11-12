package org.example.library.web.vo;

import lombok.Value;

/** 登录响应对象。 */
@Value
public class LoginResponse {

    String token;

    Long expiresIn;

    Long userId;

    String role;
}
