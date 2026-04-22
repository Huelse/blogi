package com.blogi.modules.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blogi.modules.post.entity.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    @Select("""
        SELECT id, name, slug, created_at, updated_at
        FROM tags
        WHERE LOWER(name) = LOWER(#{name})
        LIMIT 1
        """)
    PostTag findByNameIgnoreCase(String name);
}
