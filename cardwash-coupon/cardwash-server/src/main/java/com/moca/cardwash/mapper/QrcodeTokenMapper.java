package com.moca.cardwash.mapper;

import com.moca.cardwash.entity.QrcodeToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 二维码令牌 Mapper 接口
 */
@Mapper
public interface QrcodeTokenMapper {

    /**
     * 根据 token 查询
     */
    QrcodeToken selectByToken(@Param("token") String token);

    /**
     * 插入令牌
     */
    int insert(QrcodeToken token);

    /**
     * 更新令牌状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
