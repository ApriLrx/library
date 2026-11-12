package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 系统公告请求。 */
@Data
public class BroadcastRequest {

    @NotBlank(message = "content 不能为空")
    private String content;
}
