package org.example.library.storage.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.library.storage.po.NotificationPO;

import java.time.LocalDateTime;
import java.util.List;

/** 通知表 Mapper。 */
public interface NotificationMapper {

    int insert(NotificationPO po);

    NotificationPO selectById(@Param("id") Long id);

    List<NotificationPO> selectByUser(@Param("userId") Long userId,
                                      @Param("unreadOnly") boolean unreadOnly,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    long countByUser(@Param("userId") Long userId, @Param("unreadOnly") boolean unreadOnly);

    int updateRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);

    int insertBatch(@Param("list") List<NotificationPO> list);

    long countByBorrowRecordAndType(@Param("borrowRecordId") Long borrowRecordId, @Param("type") String type);
}
