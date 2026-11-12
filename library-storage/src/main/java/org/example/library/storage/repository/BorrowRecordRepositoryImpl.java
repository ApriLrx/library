package org.example.library.storage.repository;

import org.example.library.common.DomainException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageQuery;
import org.example.library.common.PageResult;
import org.example.library.domain.borrow.model.BorrowRecord;
import org.example.library.domain.borrow.model.BorrowStatus;
import org.example.library.domain.borrow.model.DueDate;
import org.example.library.domain.borrow.repository.BorrowRecordRepository;
import org.example.library.storage.mapper.BorrowRecordMapper;
import org.example.library.storage.po.BorrowRecordPO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** 借阅记录仓储实现（基础设施层）。 */
@Repository
public class BorrowRecordRepositoryImpl implements BorrowRecordRepository {

    private final BorrowRecordMapper borrowRecordMapper;

    public BorrowRecordRepositoryImpl(BorrowRecordMapper borrowRecordMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
    }

    @Override
    public Optional<BorrowRecord> findById(Long id) {
        BorrowRecordPO po = borrowRecordMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public PageResult<BorrowRecord> findByUser(Long userId, BorrowStatus status, int page, int pageSize) {
        String statusName = status == null ? null : status.name();
        PageQuery pq = PageQuery.of(page, pageSize);
        long total = borrowRecordMapper.countByUser(userId, statusName);
        List<BorrowRecord> items = borrowRecordMapper.selectByUser(userId, statusName, pq.getOffset(), pq.getEffectivePageSize())
                .stream().map(this::toDomain).collect(Collectors.toList());
        return PageResult.of(total, pq.getNormalizedPage(), pq.getEffectivePageSize(), items);
    }

    @Override
    public PageResult<BorrowRecord> findAll(String userName, int page, int pageSize) {
        PageQuery pq = PageQuery.of(page, pageSize);
        long total = borrowRecordMapper.countAll(userName);
        List<BorrowRecord> items = borrowRecordMapper.selectAll(userName, pq.getOffset(), pq.getEffectivePageSize())
                .stream().map(this::toDomain).collect(Collectors.toList());
        return PageResult.of(total, pq.getNormalizedPage(), pq.getEffectivePageSize(), items);
    }

    @Override
    public boolean existsActiveBorrow(Long userId, Long bookId) {
        return borrowRecordMapper.countActiveByUserAndBook(userId, bookId) > 0;
    }

    @Override
    public void save(BorrowRecord record) {
        BorrowRecordPO po = toPO(record);
        try {
            borrowRecordMapper.insert(po);
        } catch (DuplicateKeyException e) {
            // 活动借阅唯一索引冲突：同一用户对同一书目重复借阅
            throw new DomainException(ErrorCode.NO_STOCK, "您已借阅该书且尚未归还");
        }
        // 自增主键回填
        record.setId(po.getId());
    }

    @Override
    public int update(BorrowRecord record) {
        return borrowRecordMapper.updateStatus(toPO(record));
    }

    @Override
    public List<BorrowRecord> findDueSoon(LocalDateTime from, LocalDateTime to) {
        return borrowRecordMapper.selectDueSoon(from, to).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecord> findOverdue(LocalDateTime now) {
        return borrowRecordMapper.selectOverdue(now).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Long> findActiveBookIdsByUser(Long userId) {
        return borrowRecordMapper.selectActiveBookIdsByUser(userId);
    }

    private BorrowRecord toDomain(BorrowRecordPO po) {
        return BorrowRecord.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .bookId(po.getBookId())
                .bookName(po.getBookName())
                .userName(po.getUserName())
                .copyId(po.getCopyId())
                .borrowDate(po.getBorrowDate())
                .expectedDueDate(new DueDate(po.getExpectedDueDate()))
                .returnedAt(po.getReturnedAt())
                .status(BorrowStatus.valueOf(po.getStatus()))
                .renewCount(po.getRenewCount() == null ? 0 : po.getRenewCount())
                .operatorId(po.getOperatorId())
                .build();
    }

    private BorrowRecordPO toPO(BorrowRecord record) {
        BorrowRecordPO po = new BorrowRecordPO();
        po.setId(record.getId());
        po.setUserId(record.getUserId());
        po.setBookId(record.getBookId());
        po.setCopyId(record.getCopyId());
        po.setBorrowDate(record.getBorrowDate());
        po.setExpectedDueDate(record.getExpectedDueDate().getValue());
        po.setReturnedAt(record.getReturnedAt());
        po.setStatus(record.getStatus().name());
        po.setRenewCount(record.getRenewCount());
        po.setOperatorId(record.getOperatorId());
        return po;
    }
}
