package org.example.library.domain.borrow.service;

import org.example.library.domain.borrow.model.BorrowRecord;
import org.example.library.domain.borrow.model.BorrowStatus;

import java.time.LocalDateTime;

/** 逾期判定领域服务，供定时任务与查询复用。 */
public class OverduePolicy {

    public boolean isOverdue(BorrowRecord record, LocalDateTime now) {
        return (record.getStatus() == BorrowStatus.BORROWED || record.getStatus() == BorrowStatus.OVERDUE)
                && now.isAfter(record.getExpectedDueDate().getValue());
    }
}
