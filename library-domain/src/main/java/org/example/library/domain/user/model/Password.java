package org.example.library.domain.user.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/** 密码值对象：只持有 bcrypt/argon2 哈希结果，不存明文。 */
@Getter
@EqualsAndHashCode
public class Password implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String hash;

    public Password(String hash) {
        if (hash == null || hash.isEmpty()) {
            throw new IllegalArgumentException("密码哈希不能为空");
        }
        this.hash = hash;
    }
}
