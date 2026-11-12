package org.example.library.storage.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 书目表（books）持久化对象。 */
@Data
public class BookPO {

    private Long id;

    private String isbn;

    private String name;

    private String author;

    private String publisher;

    private Integer publishYear;

    private String description;

    private String coverUrl;

    private Long categoryId;

    private Integer totalCount;

    private Integer availableCount;

    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
