package org.example.library.domain.borrow.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 图书归还事件（骨架）。 */
@Getter
@AllArgsConstructor
public class BookReturnedEvent {

    private final Long borrowId;
}
