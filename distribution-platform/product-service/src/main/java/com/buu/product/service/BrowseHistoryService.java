package com.buu.product.service;

import com.buu.product.entity.BrowseHistory;

import java.util.List;

/**
 * 浏览历史服务
 * 提供用户商品浏览记录查询能力。
 */
public interface BrowseHistoryService {

    /**
     * 查询指定用户的浏览记录
     *
     * @param userId 用户 ID
     * @return 浏览记录列表
     */
    List<BrowseHistory> listByUserId(Long userId);
}
