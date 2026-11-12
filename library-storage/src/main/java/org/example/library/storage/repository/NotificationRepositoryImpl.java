package org.example.library.storage.repository;

import org.example.library.common.PageQuery;
import org.example.library.common.PageResult;
import org.example.library.domain.notification.model.Notification;
import org.example.library.domain.notification.model.NotificationType;
import org.example.library.domain.notification.repository.NotificationRepository;
import org.example.library.storage.mapper.NotificationMapper;
import org.example.library.storage.po.NotificationPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** 通知仓储实现（基础设施层）。 */
@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationMapper notificationMapper;

    public NotificationRepositoryImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public Optional<Notification> findById(Long id) {
        NotificationPO po = notificationMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public PageResult<Notification> findByUser(Long userId, boolean unreadOnly, int page, int pageSize) {
        PageQuery pq = PageQuery.of(page, pageSize);
        long total = notificationMapper.countByUser(userId, unreadOnly);
        List<Notification> items = notificationMapper.selectByUser(userId, unreadOnly, pq.getOffset(), pq.getEffectivePageSize())
                .stream().map(this::toDomain).collect(Collectors.toList());
        return PageResult.of(total, pq.getNormalizedPage(), pq.getEffectivePageSize(), items);
    }

    @Override
    public void updateRead(Notification notification) {
        notificationMapper.updateRead(notification.getId(), notification.getReadAt());
    }

    @Override
    public void save(Notification notification) {
        notificationMapper.insert(toPO(notification));
    }

    @Override
    public void saveAll(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        notificationMapper.insertBatch(notifications.stream().map(this::toPO).collect(Collectors.toList()));
    }

    @Override
    public boolean existsByBorrowRecordAndType(Long borrowRecordId, NotificationType type) {
        return notificationMapper.countByBorrowRecordAndType(borrowRecordId, type.name()) > 0;
    }

    private Notification toDomain(NotificationPO po) {
        return Notification.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .bookId(po.getBookId())
                .borrowRecordId(po.getBorrowRecordId())
                .type(NotificationType.valueOf(po.getType()))
                .content(po.getContent())
                .readAt(po.getReadAt())
                .createdAt(po.getCreatedAt())
                .build();
    }

    private NotificationPO toPO(Notification notification) {
        NotificationPO po = new NotificationPO();
        po.setId(notification.getId());
        po.setUserId(notification.getUserId());
        po.setBookId(notification.getBookId());
        po.setBorrowRecordId(notification.getBorrowRecordId());
        po.setType(notification.getType().name());
        po.setContent(notification.getContent());
        po.setReadAt(notification.getReadAt());
        return po;
    }
}
