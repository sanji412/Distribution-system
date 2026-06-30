package com.buu.product.service;

import com.buu.product.entity.Product;

import java.util.List;

/**
 * 商品服务
 * 提供商品基础查询与维护能力。
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

    /**
     * 新增商品
     *
     * @param product 商品信息
     * @return 新增后的商品信息
     */
    Product createProduct(Product product);

    /**
     * 更新商品
     *
     * @param productId 商品 ID
     * @param product 商品信息
     * @return 更新后的商品信息
     */
    Product updateProduct(Long productId, Product product);

    /**
     * 删除商品
     *
     * @param productId 商品 ID
     * @return 是否删除成功
     */
    boolean deleteProduct(Long productId);
}
