package com.buu.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品中心远程浏览历史响应
 * 字段与 product-center 的 BrowseHistory 实体保持一致。
 */
@Data
public class RemoteBrowseHistoryDTO {

    private Long id;
    private Long userId;
    private Long productId;
    private LocalDateTime browseTime;
}
