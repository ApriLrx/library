package org.example.library.storage.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 收藏表（favorite）持久化对象。bookName/author 为 join 查询时的冗余展示字段。 */
@Data
public class FavoritePO {

    private Long id;

    private Long userId;

    private Long bookId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** join 字段 */
    private String bookName;

    private String author;
}
