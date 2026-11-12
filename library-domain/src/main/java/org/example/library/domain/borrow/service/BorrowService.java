package org.example.library.domain.borrow.service;

import org.example.library.common.DomainException;
import org.example.library.common.ErrorCode;
import org.example.library.domain.borrow.model.BorrowPeriod;
import org.example.library.domain.borrow.model.BorrowRecord;
import org.example.library.domain.borrow.model.BorrowStatus;
import org.example.library.domain.borrow.model.DueDate;
import org.example.library.domain.borrow.repository.BorrowRecordRepository;

import java.time.LocalDateTime;

/**
 * 借阅领域服务：承载借阅记录的生命周期规则（资格校验、状态流转、落库）。
 * 不感知书目 / 用户等其他领域，跨领域协调由应用层（biz）负责。
 */
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;

    public BorrowService(BorrowRecordRepository borrowRecordRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
    }

    /** 校验借阅资格：同一用户对同一书目不得有未归还记录，且借阅期限合法。返回解析后的借阅期限。 */
    public BorrowPeriod checkLendEligibility(int days, boolean hasActiveBorrow) {
        if (hasActiveBorrow) {
            throw new DomainException(ErrorCode.NO_STOCK, "您已借阅该书且尚未归还");
        }
        try {
            return new BorrowPeriod(days);
        } catch (IllegalArgumentException e) {
            throw new DomainException(ErrorCode.PARAM_ERROR, e.getMessage());
        }
    }

    /** 生成并保存借阅记录（借出副本已由书目领域完成）。 */
    public BorrowRecord createRecord(Long userId, Long bookId, Long copyId, BorrowPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = BorrowRecord.builder()
                .userId(userId)
                .bookId(bookId)
                .copyId(copyId)
                .borrowDate(now)
                .expectedDueDate(DueDate.from(now, period.getDays()))
                .status(BorrowStatus.BORROWED)
                .renewCount(0)
                .build();
        borrowRecordRepository.save(record);
        return record;
    }

    /** 归还：状态流转为已还并落库（重复归还未落库时抛业务异常，由全局处理器映射 40902）。 */
    public BorrowRecord returnBook(BorrowRecord record, Long operatorId) {
        record.returnBack(operatorId);
        if (borrowRecordRepository.update(record) == 0) {
            throw new DomainException(ErrorCode.ALREADY_RETURNED);
        }
        return record;
    }
}
