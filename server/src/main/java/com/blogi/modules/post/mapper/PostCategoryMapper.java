package com.blogi.modules.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blogi.modules.post.entity.PostCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostCategoryMapper extends BaseMapper<PostCategory> {

    @Select("""
        SELECT id, name, slug, created_at, updated_at
        FROM categories
        WHERE LOWER(name) = LOWER(#{name})
        LIMIT 1
        """)
    PostCategory findByNameIgnoreCase(String name);
}
