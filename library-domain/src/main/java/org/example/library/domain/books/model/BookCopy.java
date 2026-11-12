package org.example.library.domain.books.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 图书册实体：条码 + 状态，随 Book 聚合一起持久化。 */
@Getter
@AllArgsConstructor
public class BookCopy {

    private final Long id;

    private final String barcode;

    private CopyStatus status;

    public void markBorrowed() {
        this.status = CopyStatus.BORROWED;
    }

    public void markAvailable() {
        this.status = CopyStatus.AVAILABLE;
    }
}
