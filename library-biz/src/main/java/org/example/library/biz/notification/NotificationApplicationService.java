package org.example.library.biz.notification;

import org.example.library.common.BusinessException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageResult;
import org.example.library.domain.books.repository.BookRepository;
import org.example.library.domain.borrow.model.BorrowRecord;
import org.example.library.domain.borrow.repository.BorrowRecordRepository;
import org.example.library.domain.favorite.repository.FavoriteRepository;
import org.example.library.domain.notification.model.Notification;
import org.example.library.domain.notification.model.NotificationType;
import org.example.library.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知应用服务：跨领域协调（借阅 / 收藏 / 书目 → 通知）与事务边界。
 * 通知的「产生」在这里编排，领域层只负责通知自身的归属与已读规则。
 */
@Service
public class NotificationApplicationService {

    private final NotificationRepository notificationRepository;

    private final FavoriteRepository favoriteRepository;

    private final BookRepository bookRepository;

    private final BorrowRecordRepository borrowRecordRepository;

    public NotificationApplicationService(NotificationRepository notificationRepository,
                                          FavoriteRepository favoriteRepository,
                                          BookRepository bookRepository,
                                          BorrowRecordRepository borrowRecordRepository) {
        this.notificationRepository = notificationRepository;
        this.favoriteRepository = favoriteRepository;
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    public PageResult<Notification> listMy(Long userId, boolean unreadOnly, int page, int pageSize) {
        return notificationRepository.findByUser(userId, unreadOnly, page, pageSize);
    }

    @Transactional
    public void markRead(Long userId, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "通知不存在"));
        if (notification.isBroadcast()) {
            // 全站公告是共享单行，不追踪个人已读
            return;
        }
        notification.markReadBy(userId);
        notificationRepository.updateRead(notification);
    }

    /** 归还后可借提醒：为所有收藏该书的读者生成 AVAILABLE 通知。 */
    @Transactional
    public void notifyBookAvailable(Long bookId) {
        List<Long> userIds = favoriteRepository.findUserIdsByBook(bookId);
        if (userIds.isEmpty()) {
            return;
        }
        String name = bookName(bookId);
        List<Notification> notifications = userIds.stream()
                .map(uid -> Notification.builder()
                        .userId(uid)
                        .bookId(bookId)
                        .type(NotificationType.AVAILABLE)
                        .content("您收藏的《" + name + "》已有可借副本，欢迎借阅")
                        .build())
                .collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
    }

    /** 每日扫描：到期前一天提醒 + 逾期标记与通知。 */
    @Transactional
    public void scanDueAndOverdue() {
        LocalDateTime now = LocalDateTime.now();

        // 到期前一天提醒
        LocalDate tomorrow = now.toLocalDate().plusDays(1);
        List<BorrowRecord> dueSoon = borrowRecordRepository.findDueSoon(
                tomorrow.atStartOfDay(), tomorrow.plusDays(1).atStartOfDay());
        for (BorrowRecord record : dueSoon) {
            if (notificationRepository.existsByBorrowRecordAndType(record.getId(), NotificationType.DUE_REMIND)) {
                continue;
            }
            notificationRepository.save(Notification.builder()
                    .userId(record.getUserId())
                    .bookId(record.getBookId())
                    .borrowRecordId(record.getId())
                    .type(NotificationType.DUE_REMIND)
                    .content("您借阅的《" + bookName(record.getBookId()) + "》将于明日到期，请及时归还")
                    .build());
        }

        // 逾期标记与通知
        List<BorrowRecord> overdue = borrowRecordRepository.findOverdue(now);
        for (BorrowRecord record : overdue) {
            record.markOverdue();
            if (borrowRecordRepository.update(record) == 0) {
                continue; // 并发下已归还，跳过
            }
            notificationRepository.save(Notification.builder()
                    .userId(record.getUserId())
                    .bookId(record.getBookId())
                    .borrowRecordId(record.getId())
                    .type(NotificationType.OVERDUE)
                    .content("您借阅的《" + bookName(record.getBookId()) + "》已逾期，请尽快归还")
                    .build());
        }
    }

    /** 管理员广播全站公告（user_id 为空）。 */
    @Transactional
    public void broadcast(String content) {
        notificationRepository.save(Notification.builder()
                .type(NotificationType.SYSTEM)
                .content(content)
                .build());
    }

    private String bookName(Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> book.getInfo().getName())
                .orElse("图书#" + bookId);
    }
}
