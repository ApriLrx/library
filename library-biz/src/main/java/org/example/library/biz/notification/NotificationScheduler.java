package org.example.library.biz.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 通知定时任务：每日扫描到期提醒与逾期。 */
@Component
public class NotificationScheduler {

    private final NotificationApplicationService notificationApplicationService;

    public NotificationScheduler(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    /** 每日 09:00 扫描：到期前一天提醒 + 逾期标记与通知。 */
    @Scheduled(cron = "${library.notification.scan-cron:0 * * * * ?}")
    public void scanDueAndOverdue() {
        notificationApplicationService.scanDueAndOverdue();
    }
}
