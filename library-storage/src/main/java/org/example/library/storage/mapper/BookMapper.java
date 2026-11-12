package org.example.library.storage.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.library.storage.po.BookPO;

import java.util.List;

/** 书目表 Mapper。 */
public interface BookMapper {

    List<BookPO> search(@Param("categoryId") Long categoryId,
                        @Param("other") boolean other,
                        @Param("keyword") String keyword,
                        @Param("onlyAvailable") Boolean onlyAvailable,
                        @Param("offset") int offset,
                        @Param("size") int size);

    long countSearch(@Param("categoryId") Long categoryId,
                     @Param("other") boolean other,
                     @Param("keyword") String keyword,
                     @Param("onlyAvailable") Boolean onlyAvailable);

    BookPO selectById(@Param("id") Long id);

    BookPO selectByIdForUpdate(@Param("id") Long id);

    int updateAvailableCount(@Param("bookId") Long bookId, @Param("availableCount") int availableCount);

    int insert(BookPO po);

    int update(BookPO po);

    int updateEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);

    long countByIsbn(@Param("isbn") String isbn);

    long countByIsbnExcludeId(@Param("isbn") String isbn, @Param("id") Long id);

    List<BookPO> selectAll(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    long countAll(@Param("keyword") String keyword);
}
