package com.buu.pay.service;

import com.buu.pay.entity.Payment;

import java.util.List;

/**
 * 支付服务
 * 提供支付单基础查询能力。
 */
public interface PaymentService {

    /**
     * 查询全部支付单
     *
     * @return 支付单列表
     */
    List<Payment> listPayments();

    /**
     * 根据订单号查询支付单
     *
     * @param orderNo 订单编号
     * @return 支付单信息，未找到时返回 null
     */
    Payment getByOrderNo(String orderNo);
}
