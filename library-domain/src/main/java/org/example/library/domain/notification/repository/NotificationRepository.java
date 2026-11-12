package org.example.library.domain.notification.repository;

import org.example.library.common.PageResult;
import org.example.library.domain.notification.model.Notification;
import org.example.library.domain.notification.model.NotificationType;

import java.util.List;
import java.util.Optional;

/** 通知仓储接口（领域层定义，基础设施层实现）。 */
public interface NotificationRepository {

    Optional<Notification> findById(Long id);

    PageResult<Notification> findByUser(Long userId, boolean unreadOnly, int page, int pageSize);

    /** 持久化已读时间（哑 UPDATE）。 */
    void updateRead(Notification notification);

    void save(Notification notification);

    /** 批量落库（一次归还通知多个收藏者）。 */
    void saveAll(List<Notification> notifications);

    /** 某借阅记录是否已生成过指定类型通知（到期提醒去重）。 */
    boolean existsByBorrowRecordAndType(Long borrowRecordId, NotificationType type);
}
