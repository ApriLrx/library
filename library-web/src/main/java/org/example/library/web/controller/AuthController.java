package org.example.library.web.controller;

import io.jsonwebtoken.Claims;
import org.example.library.biz.user.UserApplicationService;
import org.example.library.common.Result;
import org.example.library.domain.user.model.User;
import org.example.library.infra.auth.JwtUtil;
import org.example.library.infra.redis.RedisClient;
import org.example.library.infra.redis.RedisKey;
import org.example.library.web.dto.LoginRequest;
import org.example.library.web.dto.RegisterRequest;
import org.example.library.web.vo.LoginResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/** 鉴权接口。 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserApplicationService userApplicationService;

    private final JwtUtil jwtUtil;

    private final RedisClient redisClient;

    public AuthController(UserApplicationService userApplicationService, JwtUtil jwtUtil, RedisClient redisClient) {
        this.userApplicationService = userApplicationService;
        this.jwtUtil = jwtUtil;
        this.redisClient = redisClient;
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

    /** 退出登录：把当前 token 的 jti 写入 Redis 黑名单（TTL = 剩余有效期），使其立即失效。 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Result.success();
        }
        Claims claims;
        try {
            claims = jwtUtil.parse(auth.substring("Bearer ".length()));
        } catch (Exception e) {
            // token 无效则无需拉黑
            return Result.success();
        }
        String jti = claims.getId();
        Date exp = claims.getExpiration();
        long ttlSeconds = (exp.getTime() - System.currentTimeMillis()) / 1000;
        if (jti != null && ttlSeconds > 0) {
            redisClient.set(RedisKey.TOKEN_BLACKLIST.getKey(jti), "1", ttlSeconds, TimeUnit.SECONDS);
        }
        return Result.success();
    }
}
