package org.example.library.storage.repository;

import org.example.library.domain.books.model.Category;
import org.example.library.domain.books.repository.CategoryRepository;
import org.example.library.storage.mapper.CategoryMapper;
import org.example.library.storage.po.CategoryPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** 分类仓储实现（基础设施层）。 */
@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryMapper categoryMapper;

    public CategoryRepositoryImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> findEnabled() {
        return categoryMapper.selectEnabled().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Category> findAll() {
        return categoryMapper.selectAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Category> findById(Long id) {
        CategoryPO po = categoryMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryMapper.countByName(name) > 0;
    }

    @Override
    public boolean existsByNameExcludeId(String name, Long id) {
        return categoryMapper.countByNameExcludeId(name, id) > 0;
    }

    @Override
    public Long create(Category category) {
        CategoryPO po = toPO(category);
        categoryMapper.insert(po);
        return po.getId();
    }

    @Override
    public void update(Category category) {
        categoryMapper.update(toPO(category));
    }

    @Override
    public void updateEnabled(Long id, boolean enabled) {
        categoryMapper.updateEnabled(id, enabled ? 1 : 0);
    }

    private Category toDomain(CategoryPO po) {
        return Category.builder()
                .id(po.getId())
                .name(po.getName())
                .enabled(po.getEnabled() != null && po.getEnabled() == 1)
                .build();
    }

    private CategoryPO toPO(Category category) {
        CategoryPO po = new CategoryPO();
        po.setId(category.getId());
        po.setName(category.getName());
        po.setEnabled(category.isEnabled() ? 1 : 0);
        return po;
    }
}
