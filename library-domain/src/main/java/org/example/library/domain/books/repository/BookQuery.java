package org.example.library.domain.books.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.example.library.common.PageQuery;

/** 书目查询条件：分页（继承 PageQuery）+ 筛选。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BookQuery extends PageQuery {

    private Long categoryId;

    private String keyword;

    private Boolean onlyAvailable;
}
