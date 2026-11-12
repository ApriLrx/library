package org.example.library.domain.user.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/** 用户名字值对象，5-50 个字符，构造即校验。 */
@Getter
@EqualsAndHashCode
public class UserName implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;

    public UserName(String value) {
        if (value == null || value.length() < 5 || value.length() > 50) {
            throw new IllegalArgumentException("用户名长度必须为 5-50 个字符");
        }
        this.value = value;
    }
}
