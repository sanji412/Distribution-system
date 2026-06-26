package com.buu.distribution.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.distribution.entity.BrowseHistory;
import com.buu.distribution.entity.Product;
import com.buu.distribution.exception.BusinessException;
import com.buu.distribution.mapper.BrowseHistoryMapper;
import com.buu.distribution.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final BrowseHistoryMapper browseHistoryMapper;

    public List<Product> listProducts() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreateTime));
    }

    public Product getProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    public Product createProduct(Product product) {
        productMapper.insert(product);
        return product;
    }

    public Product updateProduct(Long productId, Product product) {
        product.setProductId(productId);
        if (productMapper.updateById(product) == 0) {
            throw new BusinessException("商品不存在");
        }
        return productMapper.selectById(productId);
    }

    public void deleteProduct(Long productId) {
        if (productMapper.deleteById(productId) == 0) {
            throw new BusinessException("商品不存在");
        }
    }

    public void recordBrowse(Long userId, Long productId) {
        getProduct(productId);
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setProductId(productId);
        history.setBrowseTime(LocalDateTime.now());
        browseHistoryMapper.insert(history);
    }
}
