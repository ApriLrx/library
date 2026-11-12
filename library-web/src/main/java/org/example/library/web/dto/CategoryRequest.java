package org.example.library.web.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 分类新增/编辑请求。 */
@Data
public class CategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    private String name;
}
