package com.buu.stock.controller;

import com.buu.stock.common.R;
import com.buu.stock.entity.Stock;
import com.buu.stock.entity.Warehouse;
import com.buu.stock.service.StockQueryService;
import com.buu.stock.service.WarehouseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存 Controller
 * 处理库存中心的基础查询请求。
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockQueryService stockQueryService;
    private final WarehouseService warehouseService;

    public StockController(StockQueryService stockQueryService, WarehouseService warehouseService) {
        this.stockQueryService = stockQueryService;
        this.warehouseService = warehouseService;
    }

    /**
     * 查询全部库存记录
     *
     * @return 库存列表
     */
    @GetMapping("/list")
    public R<List<Stock>> list() {
        return R.success(stockQueryService.listStocks());
    }

    /**
     * 查询指定商品库存
     *
     * @param productId 商品 ID
     * @return 商品库存列表
     */
    @GetMapping("/product/{productId}")
    public R<List<Stock>> listByProductId(@PathVariable Long productId) {
        return R.success(stockQueryService.listByProductId(productId));
    }

    /**
     * 查询全部仓库
     *
     * @return 仓库列表
     */
    @GetMapping("/warehouse/list")
    public R<List<Warehouse>> listWarehouses() {
        return R.success(warehouseService.listWarehouses());
    }
}
