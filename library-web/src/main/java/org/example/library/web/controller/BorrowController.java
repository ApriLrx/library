package org.example.library.web.controller;

import org.example.library.common.BusinessException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageResult;
import org.example.library.common.Result;
import org.example.library.domain.borrow.model.BorrowRecord;
import org.example.library.domain.borrow.model.BorrowStatus;
import org.example.library.domain.user.model.Role;
import org.example.library.infra.auth.RequireRole;
import org.example.library.infra.auth.UserContext;
import org.example.library.biz.borrow.BorrowApplicationService;
import org.example.library.web.dto.BorrowRequest;
import org.example.library.web.vo.BorrowVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/** 借阅接口：借阅/我的借阅（READER），全部借阅/归还（ADMIN）。 */
@RestController
@RequestMapping("/api/borrows")
public class BorrowController {

    private final BorrowApplicationService borrowApplicationService;

    public BorrowController(BorrowApplicationService borrowApplicationService) {
        this.borrowApplicationService = borrowApplicationService;
    }

    @RequireRole(Role.READER)
    @PostMapping
    public Result<BorrowVO> borrow(@RequestBody @Valid BorrowRequest request) {
        BorrowRecord record = borrowApplicationService.borrow(
                UserContext.getUserId(), request.getBookId(), request.getDays());
        return Result.success(BorrowVO.from(record));
    }

    @RequireRole(Role.READER)
    @GetMapping("/my")
    public Result<PageResult<BorrowVO>> my(@RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int pageSize) {
        BorrowStatus st = parseStatus(status);
        PageResult<BorrowRecord> records = borrowApplicationService.myBorrows(
                UserContext.getUserId(), st, page, pageSize);
        return Result.success(PageResult.of(records.getTotal(), records.getPage(), records.getPageSize(),
                records.getItems().stream().map(BorrowVO::from).collect(Collectors.toList())));
    }

    @RequireRole(Role.READER)
    @GetMapping("/my/book-ids")
    public Result<List<Long>> myActiveBookIds() {
        return Result.success(borrowApplicationService.myActiveBookIds(UserContext.getUserId()));
    }

    @RequireRole(Role.ADMIN)
    @GetMapping
    public Result<PageResult<BorrowVO>> list(@RequestParam(required = false) String userName,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<BorrowRecord> records = borrowApplicationService.allBorrows(userName, page, pageSize);
        return Result.success(PageResult.of(records.getTotal(), records.getPage(), records.getPageSize(),
                records.getItems().stream().map(BorrowVO::from).collect(Collectors.toList())));
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/{id}/return")
    public Result<BorrowVO> returnBook(@PathVariable Long id) {
        BorrowRecord record = borrowApplicationService.returnBook(id, UserContext.getUserId());
        return Result.success(BorrowVO.from(record));
    }

    private BorrowStatus parseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        try {
            return BorrowStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "status 参数非法");
        }
    }
}
