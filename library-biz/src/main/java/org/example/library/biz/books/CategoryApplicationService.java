package org.example.library.biz.books;

import org.example.library.common.BusinessException;
import org.example.library.common.ErrorCode;
import org.example.library.domain.books.model.Category;
import org.example.library.domain.books.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类应用服务：查询 + 管理端写操作。
 */
@Service
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;

    public CategoryApplicationService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> listEnabled() {
        return categoryRepository.findEnabled();
    }

    public List<Category> listAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Long createCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.RESOURCE_EXISTS, "分类名称已存在");
        }
        Category category = Category.builder()
                .name(name)
                .enabled(true)
                .build();
        return categoryRepository.create(category);
    }

    @Transactional
    public void updateCategory(Long id, String name) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "分类不存在"));
        if (categoryRepository.existsByNameExcludeId(name, id)) {
            throw new BusinessException(ErrorCode.RESOURCE_EXISTS, "分类名称已存在");
        }
        Category updated = Category.builder()
                .id(id)
                .name(name)
                .enabled(existing.isEnabled())
                .build();
        categoryRepository.update(updated);
    }

    @Transactional
    public void updateEnabled(Long id, boolean enabled) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "分类不存在"));
        categoryRepository.updateEnabled(id, enabled);
    }
}
