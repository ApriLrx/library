package org.example.library.domain.books.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 副本可借事件：用于触发「可借提醒」等通知（骨架，事件机制见基础设施层）。 */
@Getter
@AllArgsConstructor
public class CopyAvailableEvent {

    private final Long bookId;

    private final Long copyId;
}
