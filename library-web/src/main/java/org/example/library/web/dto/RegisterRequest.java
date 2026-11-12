package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 注册请求。 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String password;
}
