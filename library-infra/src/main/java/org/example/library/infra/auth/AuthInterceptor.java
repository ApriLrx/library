package org.example.library.infra.auth;

import io.jsonwebtoken.Claims;
import org.example.library.common.ErrorCode;
import org.example.library.domain.user.model.Role;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 鉴权拦截器（见 readme「拦截器处理链」）：
 * 1. 放行白名单（登录/注册/书籍与分类只读接口）
 * 2. 解析 Authorization: Bearer &lt;token&gt;，验签并校验 exp
 * 3. 把 userId/role 写入 {@link UserContext}
 * 4. 按 {@link RequireRole} 注解校验角色（方法级优先于类级）
 *
 * TODO: 续接 Redis 黑名单校验（token 强制下线）、借还接口限流。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (isWhitelisted(request)) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        Role role;
        try {
            Claims claims = jwtUtil.parse(auth.substring("Bearer ".length()));
            Long userId = Long.valueOf(claims.getSubject());
            role = Role.valueOf(claims.get("role", String.class));
            UserContext.set(userId, role);
        } catch (Exception e) {
            writeError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        if (!checkRole(handler, role)) {
            writeError(response, ErrorCode.FORBIDDEN);
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    private boolean checkRole(Object handler, Role role) {
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
        for (Role allowed : requireRole.value()) {
            if (allowed == role) {
                return true;
            }
        }
        return false;
    }

    private boolean isWhitelisted(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if ("POST".equals(method) && (path.equals("/api/auth/login") || path.equals("/api/auth/register"))) {
            return true;
        }
        return "GET".equals(method) && (path.startsWith("/api/books") || path.startsWith("/api/categories"));
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + errorCode.getCode()
                + ",\"message\":\"" + errorCode.getMessage() + "\",\"data\":null}");
    }
}
