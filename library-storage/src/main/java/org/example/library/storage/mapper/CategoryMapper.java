package org.example.library.storage.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.library.storage.po.CategoryPO;

import java.util.List;

/** 分类表 Mapper。 */
public interface CategoryMapper {

    List<CategoryPO> selectEnabled();

    List<CategoryPO> selectAll();

    CategoryPO selectById(@Param("id") Long id);

    int insert(CategoryPO po);

    int update(CategoryPO po);

    int updateEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);

    long countByName(@Param("name") String name);

    long countByNameExcludeId(@Param("name") String name, @Param("id") Long id);
}
