package org.example.library.storage.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 通知表（notification）持久化对象。 */
@Data
public class NotificationPO {

    private Long id;

    private Long userId;

    private Long bookId;

    private Long borrowRecordId;

    private String type;

    private String content;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
