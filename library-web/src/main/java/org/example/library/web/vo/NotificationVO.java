package org.example.library.web.vo;

import lombok.Value;

import org.example.library.domain.notification.model.Notification;
import org.example.library.domain.notification.model.NotificationType;

import java.time.LocalDateTime;

/** 通知响应对象。 */
@Value
public class NotificationVO {

    Long id;

    NotificationType type;

    String content;

    LocalDateTime readAt;

    LocalDateTime createdAt;

    boolean broadcast;

    public static NotificationVO from(Notification n) {
        return new NotificationVO(n.getId(), n.getType(), n.getContent(), n.getReadAt(), n.getCreatedAt(), n.isBroadcast());
    }
}
