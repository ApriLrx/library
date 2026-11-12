package org.example.library.domain.borrow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.library.common.DomainException;
import org.example.library.common.ErrorCode;

import java.time.LocalDateTime;

/**
 * 借阅记录聚合根：一次借阅的完整生命周期。
 * 状态机：BORROWED → RETURNED/OVERDUE/LOST；OVERDUE → RETURNED/LOST。
 */
@Getter
@Builder
@AllArgsConstructor
public class BorrowRecord {

    @Setter
    private Long id;

    private final Long userId;

    private final Long bookId;

    /** 冗余展示字段：列表查询 join 书名，写路径为空。 */
    private final String bookName;

    /** 冗余展示字段：管理端列表 join 用户名，写路径为空。 */
    private final String userName;

    private final Long copyId;

    private final LocalDateTime borrowDate;

    private final DueDate expectedDueDate;

    private LocalDateTime returnedAt;

    private BorrowStatus status;

    private final int renewCount;

    private Long operatorId;

    /** 归还：仅未归还状态可流转为已还，重复归还抛业务异常（由应用层映射 40902）。 */
    public void returnBack(Long operatorId) {
        if (status == BorrowStatus.RETURNED) {
            throw new DomainException(ErrorCode.ALREADY_RETURNED);
        }
        this.status = BorrowStatus.RETURNED;
        this.returnedAt = LocalDateTime.now();
        this.operatorId = operatorId;
    }

    public void markOverdue() {
        if (status != BorrowStatus.BORROWED) {
            throw new IllegalStateException("仅在借状态可标记逾期");
        }
        this.status = BorrowStatus.OVERDUE;
    }

    public void markLost() {
        if (status != BorrowStatus.BORROWED && status != BorrowStatus.OVERDUE) {
            throw new IllegalStateException("仅在借或逾期状态可登记丢失");
        }
        this.status = BorrowStatus.LOST;
    }
}
