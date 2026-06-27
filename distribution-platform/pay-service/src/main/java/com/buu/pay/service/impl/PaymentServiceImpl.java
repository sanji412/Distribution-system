package com.buu.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.pay.entity.Payment;
import com.buu.pay.mapper.PaymentMapper;
import com.buu.pay.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 支付服务实现
 * 基于支付 Mapper 提供支付单列表和订单支付单查询能力。
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }

    /**
     * 查询全部支付单
     *
     * @return 支付单列表
     */
    @Override
    public List<Payment> listPayments() {
        return paymentMapper.selectList(null);
    }

    /**
     * 根据订单号查询支付单
     *
     * @param orderNo 订单编号
     * @return 支付单信息，未找到时返回 null
     */
    @Override
    public Payment getByOrderNo(String orderNo) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderNo, orderNo);
        return paymentMapper.selectOne(wrapper);
    }
}
