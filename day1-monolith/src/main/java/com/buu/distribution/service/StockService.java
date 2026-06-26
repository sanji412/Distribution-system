package com.buu.distribution.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.distribution.entity.Product;
import com.buu.distribution.entity.Stock;
import com.buu.distribution.entity.Warehouse;
import com.buu.distribution.exception.BusinessException;
import com.buu.distribution.mapper.ProductMapper;
import com.buu.distribution.mapper.StockMapper;
import com.buu.distribution.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;

    public List<Warehouse> listWarehouses() {
        return warehouseMapper.selectList(new LambdaQueryWrapper<Warehouse>().orderByAsc(Warehouse::getWarehouseId));
    }

    public List<Stock> listStocks() {
        return stockMapper.selectList(new LambdaQueryWrapper<Stock>().orderByAsc(Stock::getProductId));
    }

    public List<Stock> listByProduct(Long productId) {
        return stockMapper.selectList(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getProductId, productId)
                .orderByAsc(Stock::getWarehouseId));
    }

    public Stock deduct(Long productId, Long warehouseId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("扣减数量必须大于0");
        }
        Stock stock = selectStock(productId, warehouseId);
        if (stock.getStockNum() < quantity) {
            throw new BusinessException("库存不足");
        }
        stock.setStockNum(stock.getStockNum() - quantity);
        stockMapper.updateById(stock);
        return stock;
    }

    public List<Product> listWarningProducts() {
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));
        return products.stream()
                .filter(product -> totalStock(product.getProductId()) < safeStock(product))
                .toList();
    }

    private Stock selectStock(Long productId, Long warehouseId) {
        Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getProductId, productId)
                .eq(Stock::getWarehouseId, warehouseId));
        if (stock == null) {
            throw new BusinessException("库存记录不存在");
        }
        return stock;
    }

    private int totalStock(Long productId) {
        return stockMapper.selectList(new LambdaQueryWrapper<Stock>().eq(Stock::getProductId, productId))
                .stream()
                .mapToInt(Stock::getStockNum)
                .sum();
    }

    private int safeStock(Product product) {
        return product.getSafeStock() == null ? 0 : product.getSafeStock();
    }
}
