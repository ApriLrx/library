package org.example.library.domain.borrow.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/** 借阅期限值对象：仅允许 7 / 15 / 30 天。 */
@Getter
@EqualsAndHashCode
public class BorrowPeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int days;

    public BorrowPeriod(int days) {
        if (days != 7 && days != 15 && days != 30) {
            throw new IllegalArgumentException("借阅期限只能为 7 / 15 / 30 天");
        }
        this.days = days;
    }
}
