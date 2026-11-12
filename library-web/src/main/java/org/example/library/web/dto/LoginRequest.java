package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 登录请求。 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String password;
}
