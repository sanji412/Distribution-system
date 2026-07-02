package com.example.storage.controller;

import com.example.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存对外接口
 * 提供给订单服务通过Feign远程调用
 */
@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    /**
     * 扣减库存接口
     * @param productId 商品ID
     * @param count 扣减数量
     * @return 操作结果
     */
    @PostMapping("/decrease")
    public String decrease(@RequestParam Long productId, @RequestParam Integer count) {
        storageService.decrease(productId, count);
        return "库存扣减成功";
    }
}