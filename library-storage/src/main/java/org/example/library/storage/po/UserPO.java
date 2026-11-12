package org.example.library.storage.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户表（users）持久化对象。 */
@Data
public class UserPO {

    private Long id;

    private String userName;

    private String password;

    private String role;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
