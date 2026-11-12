package org.example.library.domain.user.service;

/** 密码强度领域服务：10-20 字符，且同时包含字母与数字。 */
public class PasswordPolicy {

    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 10 || rawPassword.length() > 20) {
            throw new IllegalArgumentException("密码长度必须为 10-20 个字符");
        }
        boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("密码必须同时包含字母与数字");
        }
    }
}
