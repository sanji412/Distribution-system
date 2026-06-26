package com.buu.distribution.controller;

import com.buu.distribution.common.ApiResponse;
import com.buu.distribution.dto.PaymentCallbackRequest;
import com.buu.distribution.entity.Payment;
import com.buu.distribution.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/list")
    public ApiResponse<List<Payment>> list() {
        return ApiResponse.success(paymentService.listPayments());
    }

    @GetMapping("/order/{orderNo}")
    public ApiResponse<Payment> getByOrderNo(@PathVariable String orderNo) {
        return ApiResponse.success(paymentService.getByOrderNo(orderNo));
    }

    @PostMapping("/callback")
    public ApiResponse<Payment> callback(@RequestBody PaymentCallbackRequest request) {
        return ApiResponse.success(paymentService.callback(request));
    }
}
