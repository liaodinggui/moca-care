package com.moca.cardwash.dto.request;

import lombok.Data;

/**
 * 扫描二维码解析请求
 */
@Data
public class ScanParseRequest {

    /**
     * 二维码内容
     */
    private String qrContent;
}
