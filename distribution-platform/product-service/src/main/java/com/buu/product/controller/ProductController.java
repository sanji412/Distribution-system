package com.buu.product.controller;

import com.buu.product.common.R;
import com.buu.product.entity.BrowseHistory;
import com.buu.product.entity.Product;
import com.buu.product.service.BrowseHistoryService;
import com.buu.product.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商品 Controller
 * 处理商品中心的基础查询请求。
 */
@RestController
@RequestMapping("/api/product")
@RefreshScope
public class ProductController {

    private final ProductService productService;
    private final BrowseHistoryService browseHistoryService;
    @Value("${spring.application.name:product-center}")
    private String serviceName;
    @Value("${server.port:8002}")
    private String serverPort;
    @Value("${product.config.stock:0}")
    private Integer configStock;
    @Value("${product.config.desc:本地默认商品配置}")
    private String configDesc;

    public ProductController(ProductService productService, BrowseHistoryService browseHistoryService) {
        this.productService = productService;
        this.browseHistoryService = browseHistoryService;
    }

    /**
     * 查询全部商品
     *
     * @return 商品列表
     */
    @GetMapping("/list")
    public R<List<Product>> list() {
        return R.success(productService.listProducts());
    }

    /**
     * 根据商品 ID 查询商品
     *
     * @param productId 商品 ID
     * @return 商品信息
     */
    @GetMapping("/{productId}")
    public R<Product> detail(@PathVariable Long productId) {
        return R.success(productService.getById(productId));
    }

    /**
     * 查询指定用户的商品浏览历史
     *
     * @param userId 用户 ID
     * @return 浏览历史列表
     */
    @GetMapping("/browse-history/{userId}")
    public R<List<BrowseHistory>> listBrowseHistory(@PathVariable Long userId) {
        return R.success(browseHistoryService.listByUserId(userId));
    }

    /**
     * 查询 Nacos 配置中心演示配置
     *
     * @return 当前服务名、端口和可被 Nacos 动态覆盖的商品配置
     */
    @GetMapping("/config")
    public R<Map<String, Object>> config() {
        return R.success(Map.of(
                "serviceName", serviceName,
                "serverPort", serverPort,
                "stock", configStock,
                "desc", configDesc
        ));
    }
}
