package org.example.library.web.controller;

import org.example.library.common.PageResult;
import org.example.library.common.Result;
import org.example.library.domain.notification.model.Notification;
import org.example.library.domain.user.model.Role;
import org.example.library.infra.auth.RequireRole;
import org.example.library.infra.auth.UserContext;
import org.example.library.biz.notification.NotificationApplicationService;
import org.example.library.web.dto.BroadcastRequest;
import org.example.library.web.vo.NotificationVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.stream.Collectors;

/** 通知接口（登录用户；广播接口需管理员）。 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;

    public NotificationController(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    @RequireRole({Role.READER, Role.ADMIN})
    @GetMapping
    public Result<PageResult<NotificationVO>> list(@RequestParam(defaultValue = "false") boolean unreadOnly,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<Notification> notifications =
                notificationApplicationService.listMy(UserContext.getUserId(), unreadOnly, page, pageSize);
        return Result.success(PageResult.of(notifications.getTotal(), notifications.getPage(), notifications.getPageSize(),
                notifications.getItems().stream().map(NotificationVO::from).collect(Collectors.toList())));
    }

    @RequireRole({Role.READER, Role.ADMIN})
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationApplicationService.markRead(UserContext.getUserId(), id);
        return Result.success();
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/broadcast")
    public Result<Void> broadcast(@RequestBody @Valid BroadcastRequest request) {
        notificationApplicationService.broadcast(request.getContent());
        return Result.success();
    }
}
