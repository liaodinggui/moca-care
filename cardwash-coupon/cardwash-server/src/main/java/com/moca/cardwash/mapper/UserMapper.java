package com.moca.cardwash.mapper;

import com.moca.cardwash.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据 openid 查询用户
     */
    User selectByOpenid(@Param("openid") String openid);

    /**
     * 根据 ID 查询用户
     */
    User selectById(@Param("id") Long id);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);
}
