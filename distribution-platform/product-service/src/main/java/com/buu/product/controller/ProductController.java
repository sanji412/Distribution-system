package com.buu.product.controller;

import com.buu.product.common.R;
import com.buu.product.entity.BrowseHistory;
import com.buu.product.entity.Product;
import com.buu.product.service.BrowseHistoryService;
import com.buu.product.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品 Controller
 * 处理商品中心的基础查询请求。
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;
    private final BrowseHistoryService browseHistoryService;

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
}
