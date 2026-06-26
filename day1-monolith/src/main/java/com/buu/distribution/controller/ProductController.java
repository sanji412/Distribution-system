package com.buu.distribution.controller;

import com.buu.distribution.common.ApiResponse;
import com.buu.distribution.entity.Product;
import com.buu.distribution.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    public ApiResponse<List<Product>> list() {
        return ApiResponse.success(productService.listProducts());
    }

    @GetMapping("/{productId}")
    public ApiResponse<Product> get(@PathVariable Long productId) {
        return ApiResponse.success(productService.getProduct(productId));
    }

    @PostMapping
    public ApiResponse<Product> create(@RequestBody Product product) {
        return ApiResponse.success(productService.createProduct(product));
    }

    @PutMapping("/{productId}")
    public ApiResponse<Product> update(@PathVariable Long productId, @RequestBody Product product) {
        return ApiResponse.success(productService.updateProduct(productId, product));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> delete(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ApiResponse.success();
    }

    @PostMapping("/browse")
    public ApiResponse<Void> browse(@RequestParam Long userId, @RequestParam Long productId) {
        productService.recordBrowse(userId, productId);
        return ApiResponse.success();
    }
}
