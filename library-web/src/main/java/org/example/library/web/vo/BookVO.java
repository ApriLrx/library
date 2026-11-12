package org.example.library.web.vo;

import lombok.Value;

import org.example.library.domain.books.model.Book;

/** 书籍列表项响应对象。 */
@Value
public class BookVO {

    Long id;

    String name;

    String author;

    String coverUrl;

    Integer availableCount;

    Integer totalCount;

    Long categoryId;

    Boolean enabled;

    public static BookVO from(Book b) {
        return new BookVO(b.getId(), b.getInfo().getName(), b.getInfo().getAuthor(),
                b.getInfo().getCoverUrl(), b.getAvailableCount(), b.getTotalCount(),
                b.getCategoryId(), b.isEnabled());
    }
}
