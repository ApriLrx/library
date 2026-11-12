package org.example.library.storage.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.library.storage.po.BookCopyPO;

import java.util.List;

/** 图书册表 Mapper。 */
public interface BookCopyMapper {

    List<BookCopyPO> selectByBookId(@Param("bookId") Long bookId);

    List<BookCopyPO> selectByBookIdForUpdate(@Param("bookId") Long bookId);

    int updateStatus(@Param("copyId") Long copyId, @Param("status") String status);

    int insert(BookCopyPO po);
}
