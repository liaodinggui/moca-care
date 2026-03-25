package com.moca.cardwash.mapper;

import com.moca.cardwash.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商家 Mapper 接口
 */
@Mapper
public interface MerchantMapper {

    /**
     * 根据用户 ID 查询商家
     */
    Merchant selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 查询所有商家列表
     */
    java.util.List<Merchant> selectAllByUserId(@Param("userId") Long userId);

    /**
     * 根据 ID 查询商家
     */
    Merchant selectById(@Param("id") Long id);

    /**
     * 插入商家
     */
    int insert(Merchant merchant);

    /**
     * 更新商家
     */
    int update(Merchant merchant);
}
