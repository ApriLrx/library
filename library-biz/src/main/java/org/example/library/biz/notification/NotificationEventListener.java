package org.example.library.biz.notification;

import org.example.library.domain.books.event.CopyAvailableEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 借阅 / 书目领域事件 → 通知的异步处理器。 */
@Component
public class NotificationEventListener {

    private final NotificationApplicationService notificationApplicationService;

    public NotificationEventListener(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    /** 归还事务提交后，异步通知收藏该书的读者（可借提醒）。 */
    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCopyAvailable(CopyAvailableEvent event) {
        notificationApplicationService.notifyBookAvailable(event.getBookId());
    }
}
