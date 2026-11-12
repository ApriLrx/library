package org.example.library.domain.books.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 分类聚合根。
 * 注意：id=1 预置为「其他」分类。停用分类时数据不级联修改，
 * 展示/查询层会把「未分类或已停用分类」的图书归并到「其他」分类（见 readme 业务规则）。
 */
@Getter
@Builder
@AllArgsConstructor
public class Category {

    /** 预置「其他」分类的主键（init 脚本固定写入）。 */
    public static final long OTHER_CATEGORY_ID = 1L;

    private final Long id;

    private final String name;

    private boolean enabled;

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }
}
