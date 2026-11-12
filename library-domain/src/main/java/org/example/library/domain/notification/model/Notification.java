package org.example.library.domain.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.library.common.DomainException;
import org.example.library.common.ErrorCode;

import java.time.LocalDateTime;

/** 通知聚合（骨架）：user_id 为空表示全站公告。 */
@Getter
@Builder
@AllArgsConstructor
public class Notification {

    private final Long id;

    private final Long userId;

    private final Long bookId;

    private final Long borrowRecordId;

    private final NotificationType type;

    private final String content;

    private LocalDateTime readAt;

    private final LocalDateTime createdAt;

    public void markRead() {
        this.readAt = LocalDateTime.now();
    }

    /** 是否为全站公告（user_id 为空）。 */
    public boolean isBroadcast() {
        return this.userId == null;
    }

    /** 由指定用户标记已读，校验归属（非本人通知抛无权限）。 */
    public void markReadBy(Long userId) {
        if (this.userId != null && !this.userId.equals(userId)) {
            throw new DomainException(ErrorCode.FORBIDDEN);
        }
        markRead();
    }
}
