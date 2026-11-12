package org.example.library.domain.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.example.library.domain.user.model.UserName;

/** 用户注册事件（骨架）。 */
@Getter
@AllArgsConstructor
public class UserRegisteredEvent {

    private final Long userId;

    private final UserName userName;
}
