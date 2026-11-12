package org.example.library.storage.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.library.storage.po.BorrowRecordPO;

import java.time.LocalDateTime;
import java.util.List;

/** 借阅记录表 Mapper。 */
public interface BorrowRecordMapper {

    int insert(BorrowRecordPO po);

    int updateStatus(BorrowRecordPO po);

    BorrowRecordPO selectById(@Param("id") Long id);

    List<BorrowRecordPO> selectByUser(@Param("userId") Long userId,
                                      @Param("status") String status,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    long countByUser(@Param("userId") Long userId, @Param("status") String status);

    List<BorrowRecordPO> selectAll(@Param("userName") String userName,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    long countAll(@Param("userName") String userName);

    long countActiveByUserAndBook(@Param("userId") Long userId, @Param("bookId") Long bookId);

    List<BorrowRecordPO> selectDueSoon(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<BorrowRecordPO> selectOverdue(@Param("now") LocalDateTime now);

    List<Long> selectActiveBookIdsByUser(@Param("userId") Long userId);
}
