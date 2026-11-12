package org.example.library.domain.books.repository;

import org.example.library.domain.books.model.Category;

import java.util.List;
import java.util.Optional;

/** 分类仓储接口（领域层定义，基础设施层实现）。 */
public interface CategoryRepository {

    List<Category> findEnabled();

    List<Category> findAll();

    Optional<Category> findById(Long id);

    boolean existsByName(String name);

    boolean existsByNameExcludeId(String name, Long id);

    /** 新增分类，返回自增 id。 */
    Long create(Category category);

    /** 编辑分类名称。 */
    void update(Category category);

    /** 停用/启用。 */
    void updateEnabled(Long id, boolean enabled);
}
