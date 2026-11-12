package org.example.library.storage.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 图书册表（book_copy）持久化对象。 */
@Data
public class BookCopyPO {

    private Long id;

    private Long bookId;

    private String barcode;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
