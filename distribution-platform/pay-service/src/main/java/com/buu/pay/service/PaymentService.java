package com.buu.pay.service;

import com.buu.pay.dto.PaymentCreateRequest;
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

    /**
     * 创建支付单
     *
     * @param request 创建支付单请求
     * @return 已创建的支付单
     */
    Payment createPayment(PaymentCreateRequest request);

    /**
     * 查询当前支付库 Seata undo_log 数量
     *
     * @return undo_log 当前记录数
     */
    Long countUndoLog();
}
