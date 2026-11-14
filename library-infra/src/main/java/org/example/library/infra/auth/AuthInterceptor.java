package org.example.library.infra.auth;

import io.jsonwebtoken.Claims;
import org.example.library.common.ErrorCode;
import org.example.library.domain.user.model.Role;
import org.example.library.domain.user.model.User;
import org.example.library.domain.user.repository.UserRepository;
import org.example.library.infra.redis.RedisClient;
import org.example.library.infra.redis.RedisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * 鉴权拦截器：
 * 1. 放行白名单（登录/注册/书籍与分类公开只读接口）
 * 2. 解析 Authorization: Bearer &lt;token&gt;，验签并校验 exp
 * 3. RequireRole 角色校验（无注解的接口不拦截；角色不匹配直接 403）
 * 4. 一级黑名单：token 已被拉黑（退出登录）则拒绝
 * 5. 比对数据库校验用户状态（内部识别原因，对外统一 401 不泄露）
 * 6. 把 userId/role 写入 {@link UserContext}
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    private final RedisClient redisClient;

    public AuthInterceptor(JwtUtil jwtUtil, UserRepository userRepository, RedisClient redisClient) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.redisClient = redisClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        Claims claims;
        Long userId;
        Role role;
        try {
            claims = jwtUtil.parse(auth.substring("Bearer ".length()));
            userId = Long.valueOf(claims.getSubject());
            role = Role.valueOf(claims.get("role", String.class));
        } catch (Exception e) {
            writeError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        // 校验接口权限
        if (!checkRole(requireRole, role)) {
            writeError(response, ErrorCode.FORBIDDEN);
            return false;
        }

        // 一级黑名单：token 已拉黑则拒绝
        if (isBlacklisted(claims)) {
            writeError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        // 用户状态校验：内部识别原因，对外统一 401
        if (!checkUser(userId, role)) {
            writeError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        UserContext.set(userId, role);
        return true;
    }

    /** 一级黑名单：token 已被拉黑（退出登录）则拒绝；Redis 不可用时降级放行并记录日志。 */
    private boolean isBlacklisted(Claims claims) {
        String jti = claims.getId();
        if (jti == null) {
            return false;
        }
        try {
            return redisClient.get(RedisKey.TOKEN_BLACKLIST.getKey(jti)) != null;
        } catch (Exception e) {
            log.error("Redis 黑名单查询失败，降级放行 jti={}", jti, e);
            return false;
        }
    }

    /**
     * 比对数据库校验用户状态（防篡改 + 实时反映禁用）。
     * 具体失败原因只记日志、不外抛，避免泄露账户是否存在等信息；对外统一由调用方返回 401。
     */
    private boolean checkUser(Long userId, Role role) {
        Optional<User> optional = userRepository.findById(userId);
        if (!optional.isPresent()) {
            log.warn("token 对应账户不存在 userId={}", userId);
            return false;
        }
        User user = optional.get();
        if (!user.isActive()) {
            log.warn("账户已禁用 userId={}", userId);
            return false;
        }
        if (!role.equals(user.getRole())) {
            log.warn("token 角色与数据库不一致 userId={} tokenRole={} dbRole={}", userId, role, user.getRole());
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    private boolean checkRole(RequireRole requireRole, Role role) {
        for (Role allowed : requireRole.value()) {
            if (allowed == role) {
                return true;
            }
        }
        return false;
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + errorCode.getCode()
                + ",\"message\":\"" + errorCode.getMessage() + "\",\"data\":null}");
    }
}
