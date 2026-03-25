package com.moca.cardwash.service;

import com.moca.cardwash.entity.Merchant;
import com.moca.cardwash.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商家服务
 */
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;

    /**
     * 根据用户 ID 获取商家列表
     */
    public List<Merchant> getMerchantListByUserId(Long userId) {
        // 返回用户关联的所有商家列表
        return merchantMapper.selectAllByUserId(userId);
    }

    /**
     * 根据用户 ID 获取商家 ID
     */
    public Long getMerchantIdByUserId(Long userId) {
        Merchant merchant = merchantMapper.selectByUserId(userId);
        return merchant != null ? merchant.getId() : null;
    }
}
