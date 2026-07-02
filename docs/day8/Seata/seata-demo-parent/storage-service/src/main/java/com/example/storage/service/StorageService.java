package com.example.storage.service;

import com.example.storage.mapper.StorageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 库存业务逻辑层
 * 作为Seata的RM角色，无需添加额外事务注解
 * Seata会自动代理数据源，管理分支事务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageMapper storageMapper;

    /**
     * 扣减商品库存
     * @param productId 商品ID
     * @param count 扣减数量
     */
    public void decrease(Long productId, Integer count) {
        log.info("【库存服务】开始扣减库存，商品ID：{}，扣减数量：{}", productId, count);

        int rows = storageMapper.decreaseStorage(productId, count);
        if (rows == 0) {
            throw new RuntimeException("库存不足，扣减失败");
        }

        log.info("【库存服务】库存扣减成功");
    }
}