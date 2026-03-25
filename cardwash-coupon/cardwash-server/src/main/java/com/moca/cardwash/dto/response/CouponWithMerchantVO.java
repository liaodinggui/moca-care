package com.moca.cardwash.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 洗车券 VO（包含商家信息）
 */
@Data
public class CouponWithMerchantVO {

    /**
     * 洗车券 ID
     */
    private Long id;

    /**
     * 商家 ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 洗车券名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片 URL 数组（JSON 字符串）
     */
    private String images;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 买 X 张
     */
    private Integer buyAmount;

    /**
     * 送 Y 张
     */
    private Integer sendAmount;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
}
