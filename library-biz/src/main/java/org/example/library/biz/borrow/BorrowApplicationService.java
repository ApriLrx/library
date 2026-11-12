package org.example.library.biz.borrow;

import org.example.library.common.BusinessException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageResult;
import org.example.library.domain.books.event.CopyAvailableEvent;
import org.example.library.domain.books.model.Book;
import org.example.library.domain.books.model.BookCopy;
import org.example.library.domain.books.repository.BookRepository;
import org.example.library.domain.books.service.BookStockService;
import org.example.library.domain.borrow.model.BorrowPeriod;
import org.example.library.domain.borrow.model.BorrowRecord;
import org.example.library.domain.borrow.model.BorrowStatus;
import org.example.library.domain.borrow.repository.BorrowRecordRepository;
import org.example.library.domain.borrow.service.BorrowService;
import org.example.library.domain.user.model.User;
import org.example.library.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 借阅应用服务（biz 层）：跨领域协调（用户 / 书目 / 借阅）与事务边界。
 * 借阅资格与记录生命周期由 {@link BorrowService} 承载，书目库存变更由 {@link BookStockService} 承载。
 */
@Service
public class BorrowApplicationService {

    private final UserRepository userRepository;

    private final BookRepository bookRepository;

    private final BorrowRecordRepository borrowRecordRepository;

    private final BorrowService borrowService;

    private final BookStockService bookStockService;

    private final ApplicationEventPublisher eventPublisher;

    public BorrowApplicationService(UserRepository userRepository,
                                    BookRepository bookRepository,
                                    BorrowRecordRepository borrowRecordRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.borrowService = new BorrowService(borrowRecordRepository);
        this.bookStockService = new BookStockService(bookRepository);
        this.eventPublisher = eventPublisher;
    }

    /** 借阅：加载（锁行）→ 资格校验 → 扣库存 → 建记录，同一事务。 */
    @Transactional
    public BorrowRecord borrow(Long userId, Long bookId, int days) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在"));
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已禁用，无法借阅");
        }
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "书籍不存在"));
        if (!book.isEnabled()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "书籍已下架");
        }
        boolean hasActive = borrowRecordRepository.existsActiveBorrow(userId, bookId);
        BorrowPeriod period = borrowService.checkLendEligibility(days, hasActive);
        BookCopy copy = bookStockService.lendCopy(book);
        return borrowService.createRecord(userId, bookId, copy.getId(), period);
    }

    public PageResult<BorrowRecord> myBorrows(Long userId, BorrowStatus status, int page, int pageSize) {
        return borrowRecordRepository.findByUser(userId, status, page, pageSize);
    }

    public PageResult<BorrowRecord> allBorrows(String userName, int page, int pageSize) {
        return borrowRecordRepository.findAll(userName, page, pageSize);
    }

    /** 当前用户未归还（BORROWED/OVERDUE）借阅涉及的书目 id 列表（前端按钮态用）。 */
    public List<Long> myActiveBookIds(Long userId) {
        return borrowRecordRepository.findActiveBookIdsByUser(userId);
    }

    /** 归还：加载（锁行）→ 借阅记录状态流转 → 释放副本 → 发布可借事件，同一事务。 */
    @Transactional
    public BorrowRecord returnBook(Long borrowId, Long operatorId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "借阅记录不存在"));
        Book book = bookRepository.findByIdForUpdate(record.getBookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "书籍不存在"));
        borrowService.returnBook(record, operatorId);
        bookStockService.returnCopy(book, record.getCopyId());
        eventPublisher.publishEvent(new CopyAvailableEvent(book.getId(), record.getCopyId()));
        return record;
    }
}
