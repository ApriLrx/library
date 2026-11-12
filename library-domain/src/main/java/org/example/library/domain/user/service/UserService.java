package org.example.library.domain.user.service;

import org.example.library.common.DomainException;
import org.example.library.common.ErrorCode;
import org.example.library.domain.user.model.User;

/**
 * 用户领域服务：承载凭证与账号状态的校验规则。
 */
public class UserService {

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /** 凭证是否有效：密码匹配且账号处于正常状态（不区分具体失败原因）。 */
    public void verifyCredentials(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword().getHash())) {
            throw new DomainException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!user.isActive()) {
            throw new DomainException(ErrorCode.FORBIDDEN, "您的账户已被封禁，请联系管理员");
        }
    }
}
