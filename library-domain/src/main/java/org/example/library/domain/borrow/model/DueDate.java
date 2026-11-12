package org.example.library.domain.borrow.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 应还时间值对象：由借出时间与借期计算，逾期判定不依赖数据库字段。 */
@Getter
@EqualsAndHashCode
public class DueDate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDateTime value;

    public DueDate(LocalDateTime value) {
        this.value = value;
    }

    public static DueDate from(LocalDateTime borrowDate, int days) {
        return new DueDate(borrowDate.plusDays(days));
    }
}
