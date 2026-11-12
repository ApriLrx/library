package org.example.library.web.vo;

import lombok.Value;

import org.example.library.domain.books.model.Book;

/** 书籍详情响应对象。 */
@Value
public class BookDetailVO {

    Long id;

    String isbn;

    String name;

    String author;

    String publisher;

    Integer publishYear;

    String description;

    String coverUrl;

    Long categoryId;

    Integer availableCount;

    Integer totalCount;

    public static BookDetailVO from(Book b) {
        return new BookDetailVO(b.getId(), b.getIsbn().getValue(), b.getInfo().getName(),
                b.getInfo().getAuthor(), b.getInfo().getPublisher(), b.getInfo().getPublishYear(),
                b.getInfo().getDescription(), b.getInfo().getCoverUrl(), b.getCategoryId(),
                b.getAvailableCount(), b.getTotalCount());
    }
}
