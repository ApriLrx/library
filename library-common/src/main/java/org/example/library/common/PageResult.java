package org.example.library.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果：{@code data} 下统一使用 total/page/pageSize/items 结构。
 *
 * @param <T> 条目类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long total;

    private Integer page;

    private Integer pageSize;

    private List<T> items;

    public static <T> PageResult<T> of(Long total, int page, int pageSize, List<T> items) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setItems(items);
        return result;
    }

}
