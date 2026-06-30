package com.buu.product.service.impl;

import com.buu.product.entity.Product;
import com.buu.product.mapper.ProductMapper;
import com.buu.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品服务实现
 * 基于商品 Mapper 提供商品查询与维护能力。
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * 查询全部商品
     *
     * @return 商品列表
     */
    @Override
    public List<Product> listProducts() {
        return productMapper.selectList(null);
    }

    /**
     * 根据商品 ID 查询商品
     *
     * @param productId 商品 ID
     * @return 商品信息，未找到时返回 null
     */
    @Override
    public Product getById(Long productId) {
        return productMapper.selectById(productId);
    }

    /**
     * 新增商品
     *
     * @param product 商品信息
     * @return 新增后的商品信息
     */
    @Override
    public Product createProduct(Product product) {
        LocalDateTime now = LocalDateTime.now();
        if (product.getCreateTime() == null) {
            product.setCreateTime(now);
        }
        product.setUpdateTime(now);
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        productMapper.insert(product);
        return product;
    }

    /**
     * 更新商品
     *
     * @param productId 商品 ID
     * @param product 商品信息
     * @return 更新后的商品信息
     */
    @Override
    public Product updateProduct(Long productId, Product product) {
        product.setProductId(productId);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return product;
    }

    /**
     * 删除商品
     *
     * @param productId 商品 ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteProduct(Long productId) {
        return productMapper.deleteById(productId) > 0;
    }
}
