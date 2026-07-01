package com.buu.stock.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.buu.stock.common.R;
import com.buu.stock.dto.SentinelRuleDTO;
import com.buu.stock.entity.Stock;
import com.buu.stock.entity.Warehouse;
import com.buu.stock.service.SentinelRuleQueryService;
import com.buu.stock.service.StockQueryService;
import com.buu.stock.service.WarehouseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存 Controller
 * 处理库存中心的基础查询请求。
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockQueryService stockQueryService;
    private final WarehouseService warehouseService;
    private final SentinelRuleQueryService sentinelRuleQueryService;

    public StockController(
            StockQueryService stockQueryService,
            WarehouseService warehouseService,
            SentinelRuleQueryService sentinelRuleQueryService
    ) {
        this.stockQueryService = stockQueryService;
        this.warehouseService = warehouseService;
        this.sentinelRuleQueryService = sentinelRuleQueryService;
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
    @SentinelResource(value = "stockByProduct", blockHandler = "stockByProductBlockHandler")
    public R<List<Stock>> listByProductId(@PathVariable Long productId) {
        return R.success(stockQueryService.listByProductId(productId));
    }

    /**
     * 扣减指定商品库存
     *
     * @param productId 商品 ID
     * @param quantity  扣减数量
     * @return 扣减结果
     */
    @PostMapping("/deduct")
    @SentinelResource(value = "stockDeduct", blockHandler = "stockDeductBlockHandler")
    public R<Map<String, Object>> deductStock(@RequestParam Long productId, @RequestParam Integer quantity) {
        boolean deducted = stockQueryService.deductStock(productId, quantity);
        Map<String, Object> data = buildDeductResult(productId, quantity, deducted);
        if (!deducted) {
            return R.fail(500, "库存不足或扣减参数不合法");
        }
        return R.success(data);
    }

    /**
     * 商品库存热点参数限流回调
     *
     * @param productId  商品 ID
     * @param exception  Sentinel 限流异常
     * @return 限流提示
     */
    public R<List<Stock>> stockByProductBlockHandler(Long productId, BlockException exception) {
        return R.fail(429, "商品库存查询触发 Sentinel 热点参数限流，productId=" + productId);
    }

    /**
     * 库存扣减限流回调
     *
     * @param productId 商品 ID
     * @param quantity  扣减数量
     * @param exception Sentinel 限流异常
     * @return 限流提示
     */
    public R<Map<String, Object>> stockDeductBlockHandler(Long productId, Integer quantity, BlockException exception) {
        R<Map<String, Object>> result = R.fail(429, "库存扣减请求过于频繁，已触发 Sentinel 限流保护");
        result.setData(buildDeductResult(productId, quantity, false));
        return result;
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

    /**
     * 查询库存服务 Sentinel 规则和运行计数
     *
     * @return Sentinel 规则列表
     */
    @GetMapping("/sentinel/rules")
    public R<List<SentinelRuleDTO>> listSentinelRules() {
        return R.success(sentinelRuleQueryService.listRules());
    }

    private Map<String, Object> buildDeductResult(Long productId, Integer quantity, boolean deducted) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("productId", productId);
        data.put("quantity", quantity);
        data.put("deducted", deducted);
        return data;
    }
}
