package org.example.library.domain.favorite.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** 收藏聚合（骨架）：读者收藏某书目。bookName/author 为列表查询时的冗余展示字段。 */
@Getter
@Builder
@AllArgsConstructor
public class Favorite {

    private final Long id;

    private final Long userId;

    private final Long bookId;

    private final String bookName;

    private final String author;
}
