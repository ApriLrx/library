package org.example.library.storage.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 借阅记录表（borrow_record）持久化对象。 */
@Data
public class BorrowRecordPO {

    private Long id;

    private Long userId;

    private Long bookId;

    /** join 字段 */
    private String bookName;

    /** join 字段 */
    private String userName;

    private Long copyId;

    private LocalDateTime borrowDate;

    private LocalDateTime expectedDueDate;

    private LocalDateTime returnedAt;

    private String status;

    private Integer renewCount;

    private Long operatorId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
