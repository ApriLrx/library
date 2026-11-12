package org.example.library.domain.user.service;

/**
 * 密码哈希端口：领域层定义，基础设施层提供实现（bcrypt 等）。
 */
public interface PasswordEncoder {

    /** 生成密码哈希（加盐，不可逆）。 */
    String encode(String rawPassword);

    /** 校验明文密码与哈希是否匹配。 */
    boolean matches(String rawPassword, String encodedPassword);
}
