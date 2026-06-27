package com.buu.product.service;

import com.buu.product.entity.Product;

import java.util.List;

/**
 * 商品服务
 * 提供商品基础查询能力。
 */
public interface ProductService {

    /**
     * 查询全部商品
     *
     * @return 商品列表
     */
    List<Product> listProducts();

    /**
     * 根据商品 ID 查询商品
     *
     * @param productId 商品 ID
     * @return 商品信息，未找到时返回 null
     */
    Product getById(Long productId);
}
