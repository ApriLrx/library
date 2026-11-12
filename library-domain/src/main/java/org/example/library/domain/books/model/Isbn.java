package org.example.library.domain.books.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/** ISBN 值对象，构造即校验非空。 */
@Getter
@EqualsAndHashCode
public class Isbn implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;

    public Isbn(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN 不能为空");
        }
        this.value = value.trim();
    }
}
