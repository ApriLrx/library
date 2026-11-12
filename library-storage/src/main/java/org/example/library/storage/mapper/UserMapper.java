package org.example.library.storage.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.library.storage.po.UserPO;

import java.util.List;

/** 用户表 Mapper。 */
public interface UserMapper {

    UserPO selectByUserName(@Param("userName") String userName);

    UserPO selectById(@Param("id") Long id);

    int insert(UserPO po);

    List<UserPO> selectAll(@Param("offset") int offset, @Param("size") int size);

    long countAll();

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
