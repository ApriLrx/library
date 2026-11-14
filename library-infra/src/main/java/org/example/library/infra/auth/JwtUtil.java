package org.example.library.infra.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.example.library.domain.user.model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * 无状态 JWT（HS256）工具：载荷只放 sub(用户id) 与 role，不落敏感信息。
 */
@Component
public class JwtUtil {

    private final String secret;

    @Getter
    private final long expireSeconds;

    public JwtUtil(@Value("${library.jwt.secret}") String secret,
                   @Value("${library.jwt.expire-seconds:7200}") long expireSeconds) {
        this.secret = secret;
        this.expireSeconds = expireSeconds;
    }

    public String generate(Long userId, Role role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(String.valueOf(userId))
                .claim("role", role.name())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireSeconds * 1000))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    /** 验签并解析，失败抛出 {@link io.jsonwebtoken.JwtException} 由调用方处理。 */
    public Claims parse(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}
