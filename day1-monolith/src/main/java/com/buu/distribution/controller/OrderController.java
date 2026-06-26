package com.buu.distribution.controller;

import com.buu.distribution.common.ApiResponse;
import com.buu.distribution.dto.CreateOrderRequest;
import com.buu.distribution.dto.DashboardResponse;
import com.buu.distribution.dto.OrderDetailResponse;
import com.buu.distribution.dto.UpdateOrderStatusRequest;
import com.buu.distribution.entity.OrderMain;
import com.buu.distribution.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    @GetMapping("/list")
    public ApiResponse<List<OrderMain>> list() {
        return ApiResponse.success(orderService.listOrders());
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderDetailResponse> get(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.getByOrderNo(orderNo));
    }

    @PostMapping("/create")
    public ApiResponse<OrderDetailResponse> create(@RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    @PutMapping("/status")
    public ApiResponse<OrderDetailResponse> updateStatus(@RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.success(orderService.updateStatus(request));
    }

    @GetMapping("/dashboard/summary")
    public ApiResponse<DashboardResponse> dashboard() {
        return ApiResponse.success(orderService.dashboard());
    }
}
