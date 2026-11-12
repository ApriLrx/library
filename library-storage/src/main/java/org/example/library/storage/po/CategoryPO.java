package org.example.library.storage.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 分类表（categories）持久化对象。 */
@Data
public class CategoryPO {

    private Long id;

    private String name;

    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
