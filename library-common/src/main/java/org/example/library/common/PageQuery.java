package org.example.library.common;

import lombok.Data;

/**
 * 分页查询参数：page 从 1 开始，pageSize 默认 20、上限 100。
 */
@Data
public class PageQuery {

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int MAX_PAGE_SIZE = 100;

    private Integer page = 1;

    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /** 归一化工厂：供仓储对裸 page/pageSize 参数统一归一化。 */
    public static PageQuery of(int page, int pageSize) {
        PageQuery q = new PageQuery();
        q.setPage(page < 1 ? 1 : page);
        q.setPageSize(pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE));
        return q;
    }

    /** 归一到合法范围后的页码（≥1）。 */
    public int getNormalizedPage() {
        return page == null || page < 1 ? 1 : page;
    }

    /** 数据库 offset，用于 LIMIT offset, size。 */
    public int getOffset() {
        return (getNormalizedPage() - 1) * getEffectivePageSize();
    }

    /** 归一到合法范围后的每页条数。 */
    public int getEffectivePageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

}
