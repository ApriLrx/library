package org.example.library.web.vo;

import lombok.Value;

import org.example.library.domain.favorite.model.Favorite;

/** 收藏响应对象。 */
@Value
public class FavoriteVO {

    Long id;

    Long bookId;

    String bookName;

    String author;

    public static FavoriteVO from(Favorite f) {
        return new FavoriteVO(f.getId(), f.getBookId(), f.getBookName(), f.getAuthor());
    }
}
