package org.example.library.domain.favorite.repository;

import org.example.library.common.PageResult;
import org.example.library.domain.favorite.model.Favorite;

import java.util.List;

/** 收藏仓储接口（领域层定义，基础设施层实现）。 */
public interface FavoriteRepository {

    void add(Favorite favorite);

    void remove(Long userId, Long bookId);

    boolean exists(Long userId, Long bookId);

    PageResult<Favorite> findByUser(Long userId, int page, int pageSize);

    /** 收藏某书目的用户 id 列表（可借提醒用）。 */
    List<Long> findUserIdsByBook(Long bookId);

    /** 某用户收藏的书目 id 列表。 */
    List<Long> findBookIdsByUser(Long userId);
}
