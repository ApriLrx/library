package org.example.library.domain.borrow.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 借阅创建事件（骨架）。 */
@Getter
@AllArgsConstructor
public class BorrowCreatedEvent {

    private final Long borrowId;
}
