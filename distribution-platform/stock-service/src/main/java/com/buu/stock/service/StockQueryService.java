package com.buu.stock.service;

import com.buu.stock.entity.Stock;

import java.util.List;

/**
 * 库存查询服务
 * 提供库存列表和商品库存查询能力。
 */
public interface StockQueryService {

    /**
     * 查询全部库存记录
     *
     * @return 库存列表
     */
    List<Stock> listStocks();

    /**
     * 查询指定商品的库存记录
     *
     * @param productId 商品 ID
     * @return 商品库存列表
     */
    List<Stock> listByProductId(Long productId);
}
