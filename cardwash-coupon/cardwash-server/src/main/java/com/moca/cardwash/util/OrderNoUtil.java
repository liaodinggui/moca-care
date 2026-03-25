package com.moca.cardwash.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单号生成工具类
 */
public class OrderNoUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 生成订单号
     * 格式：CW + 年月日时分秒 + 4 位随机数
     */
    public static String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "CW" + timestamp + randomNum;
    }
}
