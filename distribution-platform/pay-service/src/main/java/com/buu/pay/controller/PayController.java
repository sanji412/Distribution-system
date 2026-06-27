package com.buu.pay.controller;

import com.buu.pay.common.R;
import com.buu.pay.entity.Payment;
import com.buu.pay.service.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支付 Controller
 * 处理支付中心的基础查询请求。
 */
@RestController
@RequestMapping("/api/pay")
public class PayController {

    private final PaymentService paymentService;

    public PayController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 查询全部支付单
     *
     * @return 支付单列表
     */
    @GetMapping("/list")
    public R<List<Payment>> list() {
        return R.success(paymentService.listPayments());
    }

    /**
     * 根据订单号查询支付单
     *
     * @param orderNo 订单编号
     * @return 支付单信息
     */
    @GetMapping("/order/{orderNo}")
    public R<Payment> detailByOrderNo(@PathVariable String orderNo) {
        return R.success(paymentService.getByOrderNo(orderNo));
    }
}
