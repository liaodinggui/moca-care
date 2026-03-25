package com.moca.cardwash.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 核销结果响应
 */
@Data
@Builder
public class WriteOffResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 是否已完成（所有券已核销）
     */
    private Boolean completed;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 剩余数量
     */
    private Integer remainingQty;
}
