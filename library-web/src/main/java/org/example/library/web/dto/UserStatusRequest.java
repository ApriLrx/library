package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 用户启用/禁用请求。 */
@Data
public class UserStatusRequest {

    @NotNull(message = "active 不能为空")
    private Boolean active;
}
