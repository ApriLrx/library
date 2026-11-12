package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 收藏请求。 */
@Data
public class FavoriteRequest {

    @NotNull(message = "bookId 不能为空")
    private Long bookId;
}
