package org.example.library.web.controller;

import org.example.library.common.Result;
import org.example.library.domain.user.model.Role;
import org.example.library.infra.auth.RequireRole;
import org.example.library.biz.books.CategoryApplicationService;
import org.example.library.web.dto.CategoryRequest;
import org.example.library.web.dto.EnabledRequest;
import org.example.library.web.vo.CategoryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/** 分类接口：读公开，写需 ADMIN。 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryApplicationService categoryApplicationService;

    public CategoryController(CategoryApplicationService categoryApplicationService) {
        this.categoryApplicationService = categoryApplicationService;
    }

    @GetMapping
    public Result<List<CategoryVO>> list() {
        List<CategoryVO> vos = categoryApplicationService.listEnabled().stream()
                .map(CategoryVO::from).collect(Collectors.toList());
        return Result.success(vos);
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/all")
    public Result<List<CategoryVO>> listAll() {
        List<CategoryVO> vos = categoryApplicationService.listAll().stream()
                .map(CategoryVO::from).collect(Collectors.toList());
        return Result.success(vos);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping
    public Result<Long> create(@RequestBody @Valid CategoryRequest request) {
        return Result.success(categoryApplicationService.createCategory(request.getName()));
    }

    @RequireRole(Role.ADMIN)
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CategoryRequest request) {
        categoryApplicationService.updateCategory(id, request.getName());
        return Result.success();
    }

    @RequireRole(Role.ADMIN)
    @PatchMapping("/{id}/enabled")
    public Result<Void> updateEnabled(@PathVariable Long id, @RequestBody @Valid EnabledRequest request) {
        categoryApplicationService.updateEnabled(id, request.getEnabled());
        return Result.success();
    }
}
