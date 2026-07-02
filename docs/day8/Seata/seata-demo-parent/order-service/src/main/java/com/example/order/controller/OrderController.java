package com.example.order.controller;

import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 订单对外接口
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单接口
     * @param userId 用户ID
     * @param productId 商品ID
     * @param count 购买数量
     * @param money 订单金额
     * @return 操作结果
     */
    @PostMapping("/create")
    public String createOrder(@RequestParam Long userId,
                              @RequestParam Long productId,
                              @RequestParam Integer count,
                              @RequestParam BigDecimal money) {
        orderService.createOrder(userId, productId, count, money);
        return "订单创建成功，分布式事务执行完成";
    }
}