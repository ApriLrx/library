package org.example.library.web.controller;

import org.example.library.common.Result;
import org.example.library.domain.user.model.User;
import org.example.library.infra.auth.JwtUtil;
import org.example.library.biz.user.UserApplicationService;
import org.example.library.web.dto.LoginRequest;
import org.example.library.web.dto.RegisterRequest;
import org.example.library.web.vo.LoginResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/** 鉴权接口。 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserApplicationService userApplicationService;

    private final JwtUtil jwtUtil;

    public AuthController(UserApplicationService userApplicationService, JwtUtil jwtUtil) {
        this.userApplicationService = userApplicationService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest request) {
        userApplicationService.register(request.getUserName(), request.getPassword());
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = userApplicationService.login(request.getUserName(), request.getPassword());
        String token = jwtUtil.generate(user.getId(), user.getRole());
        LoginResponse response = new LoginResponse(
                token, jwtUtil.getExpireSeconds(), user.getId(), user.getRole().name());
        return Result.success(response);
    }
}
