package org.example.library.web.vo;

import lombok.Value;

import org.example.library.domain.books.model.Category;

/** 分类响应对象。 */
@Value
public class CategoryVO {

    Long id;

    String name;

    Boolean enabled;

    public static CategoryVO from(Category c) {
        return new CategoryVO(c.getId(), c.getName(), c.isEnabled());
    }
}
