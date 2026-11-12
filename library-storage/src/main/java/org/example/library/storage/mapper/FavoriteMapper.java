package org.example.library.storage.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.library.storage.po.FavoritePO;

import java.util.List;

/** 收藏表 Mapper。 */
public interface FavoriteMapper {

    int insert(FavoritePO po);

    int delete(@Param("userId") Long userId, @Param("bookId") Long bookId);

    long countByUser(@Param("userId") Long userId);

    List<FavoritePO> selectByUser(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    long countByUserAndBook(@Param("userId") Long userId, @Param("bookId") Long bookId);

    List<Long> selectUserIdsByBook(@Param("bookId") Long bookId);

    List<Long> selectBookIdsByUser(@Param("userId") Long userId);
}
