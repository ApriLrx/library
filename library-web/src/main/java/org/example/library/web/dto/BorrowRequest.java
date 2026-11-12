package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 借阅请求。 */
@Data
public class BorrowRequest {

    @NotNull(message = "bookId 不能为空")
    private Long bookId;

    @NotNull(message = "借阅天数不能为空")
    private Integer days;
}
