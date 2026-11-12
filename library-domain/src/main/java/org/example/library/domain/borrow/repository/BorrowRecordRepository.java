package org.example.library.domain.borrow.repository;

import org.example.library.common.PageResult;
import org.example.library.domain.borrow.model.BorrowRecord;
import org.example.library.domain.borrow.model.BorrowStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** 借阅记录仓储接口（领域层定义，基础设施层实现）。 */
public interface BorrowRecordRepository {

    Optional<BorrowRecord> findById(Long id);

    PageResult<BorrowRecord> findByUser(Long userId, BorrowStatus status, int page, int pageSize);

    PageResult<BorrowRecord> findAll(String userName, int page, int pageSize);

    /** 该用户对该书目是否存在未归还（BORROWED/OVERDUE）记录。 */
    boolean existsActiveBorrow(Long userId, Long bookId);

    /** 新增借阅记录。 */
    void save(BorrowRecord record);

    /** 更新可变字段（状态、归还时间、经办人），返回影响行数（用于幂等判断）。 */
    int update(BorrowRecord record);

    /** 到期提醒：status=BORROWED 且应还时间落在 [from, to) 的借阅。 */
    List<BorrowRecord> findDueSoon(LocalDateTime from, LocalDateTime to);

    /** 逾期：status=BORROWED 且应还时间早于 now 的借阅。 */
    List<BorrowRecord> findOverdue(LocalDateTime now);

    /** 该用户当前未归还（BORROWED/OVERDUE）借阅涉及的书目 id 列表。 */
    List<Long> findActiveBookIdsByUser(Long userId);
}
