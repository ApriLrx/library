package org.example.library.infra.security;

import org.example.library.domain.user.service.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * bcrypt 密码哈希实现（cost 10），实现领域层的 {@link PasswordEncoder} 端口。
 */
@Component
public class BCryptPasswordEncoder implements PasswordEncoder {

    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder delegate =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10);

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
