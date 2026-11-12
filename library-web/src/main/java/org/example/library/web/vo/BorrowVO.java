package org.example.library.web.vo;

import lombok.Value;

import org.example.library.domain.borrow.model.BorrowRecord;
import org.example.library.domain.borrow.model.BorrowStatus;

import java.time.LocalDateTime;

/** 借阅记录响应对象。 */
@Value
public class BorrowVO {

    Long borrowId;

    Long bookId;

    String bookName;

    String userName;

    Long copyId;

    LocalDateTime borrowDate;

    LocalDateTime expectedDueDate;

    LocalDateTime returnedAt;

    BorrowStatus status;

    Integer renewCount;

    public static BorrowVO from(BorrowRecord r) {
        return new BorrowVO(r.getId(), r.getBookId(), r.getBookName(), r.getUserName(), r.getCopyId(), r.getBorrowDate(),
                r.getExpectedDueDate().getValue(), r.getReturnedAt(), r.getStatus(), r.getRenewCount());
    }
}
