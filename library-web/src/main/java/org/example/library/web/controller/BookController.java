package org.example.library.web.controller;

import org.example.library.common.BusinessException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageResult;
import org.example.library.common.Result;
import org.example.library.domain.books.model.Book;
import org.example.library.domain.books.repository.BookQuery;
import org.example.library.domain.user.model.Role;
import org.example.library.infra.auth.RequireRole;
import org.example.library.biz.books.BookApplicationService;
import org.example.library.web.dto.BookCreateRequest;
import org.example.library.web.dto.BookUpdateRequest;
import org.example.library.web.dto.EnabledRequest;
import org.example.library.web.vo.BookDetailVO;
import org.example.library.web.vo.BookVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.stream.Collectors;

/** 书籍接口：读公开，写需 ADMIN。 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookApplicationService bookApplicationService;

    public BookController(BookApplicationService bookApplicationService) {
        this.bookApplicationService = bookApplicationService;
    }

    @GetMapping
    public Result<PageResult<BookVO>> list(BookQuery query) {
        PageResult<Book> page = bookApplicationService.search(query);
        PageResult<BookVO> voPage = PageResult.of(
                page.getTotal(), page.getPage(), page.getPageSize(),
                page.getItems().stream().map(BookVO::from).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/all")
    public Result<PageResult<BookVO>> listAll(@RequestParam(required = false) String keyword,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<Book> pageResult = bookApplicationService.listAll(keyword, page, pageSize);
        PageResult<BookVO> voPage = PageResult.of(pageResult.getTotal(), pageResult.getPage(), pageResult.getPageSize(),
                pageResult.getItems().stream().map(BookVO::from).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    @GetMapping("/{id}")
    public Result<BookDetailVO> detail(@PathVariable Long id) {
        return bookApplicationService.detail(id)
                .map(BookDetailVO::from)
                .map(Result::success)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    @RequireRole(Role.ADMIN)
    @PostMapping
    public Result<Long> create(@RequestBody @Valid BookCreateRequest request) {
        Long id = bookApplicationService.createBook(request.getIsbn(), request.getName(), request.getAuthor(),
                request.getPublisher(), request.getPublishYear(), request.getDescription(), request.getCoverUrl(),
                request.getCategoryId(), request.getTotalCount());
        return Result.success(id);
    }

    @RequireRole(Role.ADMIN)
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid BookUpdateRequest request) {
        bookApplicationService.updateBook(id, request.getName(), request.getAuthor(), request.getPublisher(),
                request.getPublishYear(), request.getDescription(), request.getCoverUrl(), request.getCategoryId());
        return Result.success();
    }

    @RequireRole(Role.ADMIN)
    @PatchMapping("/{id}/enabled")
    public Result<Void> updateEnabled(@PathVariable Long id, @RequestBody @Valid EnabledRequest request) {
        bookApplicationService.updateEnabled(id, request.getEnabled());
        return Result.success();
    }
}
