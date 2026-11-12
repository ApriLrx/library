package org.example.library.web.controller;

import org.example.library.common.PageResult;
import org.example.library.common.Result;
import org.example.library.domain.user.model.Role;
import org.example.library.domain.user.model.User;
import org.example.library.infra.auth.RequireRole;
import org.example.library.biz.user.UserApplicationService;
import org.example.library.web.dto.UserStatusRequest;
import org.example.library.web.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.stream.Collectors;

/** 用户接口（ADMIN）。 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @RequireRole(Role.ADMIN)
    @GetMapping
    public Result<PageResult<UserVO>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<User> users = userApplicationService.listUsers(page, pageSize);
        return Result.success(PageResult.of(users.getTotal(), users.getPage(), users.getPageSize(),
                users.getItems().stream().map(UserVO::from).collect(Collectors.toList())));
    }

    @RequireRole(Role.ADMIN)
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid UserStatusRequest request) {
        userApplicationService.updateStatus(id, request.getActive());
        return Result.success();
    }
}
