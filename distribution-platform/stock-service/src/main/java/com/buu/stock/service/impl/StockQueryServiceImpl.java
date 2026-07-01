package com.buu.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.stock.entity.Stock;
import com.buu.stock.mapper.StockMapper;
import com.buu.stock.service.StockQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存查询服务实现
 * 基于库存 Mapper 提供库存列表和商品库存查询能力。
 */
@Service
public class StockQueryServiceImpl implements StockQueryService {

    private final StockMapper stockMapper;

    public StockQueryServiceImpl(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    /**
     * 查询全部库存记录
     *
     * @return 库存列表
     */
    @Override
    public List<Stock> listStocks() {
        return stockMapper.selectList(null);
    }

    /**
     * 查询指定商品的库存记录
     *
     * @param productId 商品 ID
     * @return 商品库存列表
     */
    @Override
    public List<Stock> listByProductId(Long productId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getProductId, productId);
        return stockMapper.selectList(wrapper);
    }

    /**
     * 扣减指定商品库存
     *
     * @param productId 商品 ID
     * @param quantity  扣减数量
     * @return 是否扣减成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            return false;
        }

        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getProductId, productId)
                .ge(Stock::getStockNum, quantity)
                .orderByDesc(Stock::getStockNum)
                .last("LIMIT 1");

        Stock stock = stockMapper.selectOne(wrapper);
        if (stock == null) {
            return false;
        }

        stock.setStockNum(stock.getStockNum() - quantity);
        stock.setUpdateTime(LocalDateTime.now());
        return stockMapper.updateById(stock) > 0;
    }
}
