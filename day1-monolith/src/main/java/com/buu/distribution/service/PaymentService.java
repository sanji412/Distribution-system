package com.buu.distribution.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.distribution.dto.PaymentCallbackRequest;
import com.buu.distribution.entity.OrderMain;
import com.buu.distribution.entity.Payment;
import com.buu.distribution.exception.BusinessException;
import com.buu.distribution.mapper.OrderMainMapper;
import com.buu.distribution.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMainMapper orderMainMapper;

    public List<Payment> listPayments() {
        return paymentMapper.selectList(new LambdaQueryWrapper<Payment>().orderByDesc(Payment::getCreateTime));
    }

    public Payment getByOrderNo(String orderNo) {
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>().eq(Payment::getOrderNo, orderNo));
        if (payment == null) {
            throw new BusinessException("支付单不存在");
        }
        return payment;
    }

    @Transactional(rollbackFor = Exception.class)
    public Payment callback(PaymentCallbackRequest request) {
        Payment payment = getByOrderNo(request.getOrderNo());
        payment.setPayStatus(request.getPayStatus());
        payment.setPayMethod(request.getPayMethod());
        payment.setCallbackContent(request.getCallbackContent());
        payment.setCallbackTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        OrderMain order = orderMainMapper.selectOne(new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, request.getOrderNo()));
        if (order != null && "支付成功".equals(request.getPayStatus())) {
            order.setOrderStatus("已支付");
            order.setPayTime(LocalDateTime.now());
            orderMainMapper.updateById(order);
        }
        return payment;
    }
}
