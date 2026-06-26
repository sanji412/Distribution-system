package com.buu.distribution.controller;

import com.buu.distribution.common.ApiResponse;
import com.buu.distribution.entity.Product;
import com.buu.distribution.entity.Stock;
import com.buu.distribution.entity.Warehouse;
import com.buu.distribution.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/warehouses")
    public ApiResponse<List<Warehouse>> warehouses() {
        return ApiResponse.success(stockService.listWarehouses());
    }

    @GetMapping("/list")
    public ApiResponse<List<Stock>> list() {
        return ApiResponse.success(stockService.listStocks());
    }

    @GetMapping("/product")
    public ApiResponse<List<Stock>> byProduct(@RequestParam Long productId) {
        return ApiResponse.success(stockService.listByProduct(productId));
    }

    @PostMapping("/deduct")
    public ApiResponse<Stock> deduct(@RequestParam Long productId, @RequestParam Long warehouseId, @RequestParam Integer quantity) {
        return ApiResponse.success(stockService.deduct(productId, warehouseId, quantity));
    }

    @GetMapping("/warnings")
    public ApiResponse<List<Product>> warnings() {
        return ApiResponse.success(stockService.listWarningProducts());
    }
}
