package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 上下架/启停请求。 */
@Data
public class EnabledRequest {

    @NotNull(message = "enabled 不能为空")
    private Boolean enabled;
}
