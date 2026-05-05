package com.blogi.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blogi.modules.auth.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    @Select("""
        SELECT id, username, display_name, avatar_url, password_hash, created_at, updated_at
        FROM users
        WHERE username = #{username}
        LIMIT 1
        """)
    UserAccount findByUsername(String username);
}
