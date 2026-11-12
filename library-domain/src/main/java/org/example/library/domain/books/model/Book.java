package org.example.library.domain.books.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.library.common.DomainException;
import org.example.library.common.ErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 书目聚合根：持有书目信息、可借数量与副本集合。
 * 借出/归还只能通过本聚合修改，保证 {@code availableCount} 与副本状态一致。
 */
@Getter
@Builder
@AllArgsConstructor
public class Book {

    private final Long id;

    private final Isbn isbn;

    private final BookInfo info;

    private final Long categoryId;

    private final int totalCount;

    private int availableCount;

    private final boolean enabled;

    @Builder.Default
    private final List<BookCopy> copies = new ArrayList<>();

    /**
     * 静态工厂：按 {@code totalCount} 生成 AVAILABLE 副本，条码 = isbn-序号，
     * 初始可借数量等于馆藏总量。
     */
    public static Book createNew(Isbn isbn, BookInfo info, Long categoryId, int totalCount) {
        if (totalCount <= 0) {
            throw new DomainException(ErrorCode.PARAM_ERROR, "馆藏数量必须大于 0");
        }
        List<BookCopy> copies = new ArrayList<>(totalCount);
        for (int i = 1; i <= totalCount; i++) {
            copies.add(new BookCopy(null, isbn.getValue() + "-" + i, CopyStatus.AVAILABLE));
        }
        return Book.builder()
                .isbn(isbn)
                .info(info)
                .categoryId(categoryId)
                .totalCount(totalCount)
                .availableCount(totalCount)
                .enabled(true)
                .copies(copies)
                .build();
    }

    /** 借出：自选一个 AVAILABLE 副本，置 BORROWED，可借数量 -1。 */
    public BookCopy lend() {
        if (availableCount <= 0) {
            throw new DomainException(ErrorCode.NO_STOCK);
        }
        BookCopy copy = copies.stream()
                .filter(c -> c.getStatus() == CopyStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.NO_STOCK));
        copy.markBorrowed();
        this.availableCount--;
        return copy;
    }

    /** 归还：副本恢复可借，可借数量 +1。 */
    public void giveBack(Long copyId) {
        BookCopy copy = copies.stream()
                .filter(c -> copyId.equals(c.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "副本不存在"));
        if (copy.getStatus() != CopyStatus.BORROWED) {
            throw new DomainException(ErrorCode.SYSTEM_ERROR, "副本状态不一致，无法归还");
        }
        copy.markAvailable();
        this.availableCount++;
    }

    /** 更新书目基本信息（书名/作者/出版社/简介/分类），保留 isbn、库存与上下架状态。 */
    public Book withInfo(BookInfo info, Long categoryId) {
        return Book.builder()
                .id(this.id)
                .isbn(this.isbn)
                .info(info)
                .categoryId(categoryId)
                .totalCount(this.totalCount)
                .availableCount(this.availableCount)
                .enabled(this.enabled)
                .copies(this.copies)
                .build();
    }

    public boolean isAvailable() {
        return enabled && availableCount > 0;
    }
}
