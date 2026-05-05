package com.blogi.modules.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blogi.modules.post.entity.PostArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostArticleMapper extends BaseMapper<PostArticle> {

    @Update("""
        UPDATE posts
        SET view_count = view_count + #{delta}
        WHERE id = #{postId}
        """)
    int incrementViewCount(@Param("postId") Long postId, @Param("delta") long delta);
}
