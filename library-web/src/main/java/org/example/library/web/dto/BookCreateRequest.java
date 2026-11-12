package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** 新增书籍请求。 */
@Data
public class BookCreateRequest {

    @NotBlank(message = "ISBN 不能为空")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    private String name;

    @NotBlank(message = "作者不能为空")
    private String author;

    private String publisher;

    private Integer publishYear;

    private String description;

    private String coverUrl;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "馆藏数量不能为空")
    private Integer totalCount;
}
