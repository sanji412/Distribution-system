package com.buu.product.service.impl;

import com.buu.product.entity.Product;
import com.buu.product.mapper.ProductMapper;
import com.buu.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现
 * 基于商品 Mapper 提供商品查询能力。
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
}
