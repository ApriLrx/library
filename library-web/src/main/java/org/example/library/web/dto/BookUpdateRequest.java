package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** 编辑书籍请求（不含库存/上下架）。 */
@Data
public class BookUpdateRequest {

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
}
