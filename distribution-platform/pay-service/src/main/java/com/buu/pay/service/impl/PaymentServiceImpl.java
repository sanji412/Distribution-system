package com.buu.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.pay.dto.PaymentCreateRequest;
import com.buu.pay.entity.Payment;
import com.buu.pay.mapper.PaymentMapper;
import com.buu.pay.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 支付服务实现
 * 基于支付 Mapper 提供支付单列表和订单支付单查询能力。
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final DateTimeFormatter PAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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

    /**
     * 创建支付单
     *
     * @param request 创建支付单请求
     * @return 已创建的支付单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment createPayment(PaymentCreateRequest request) {
        validateRequest(request);
        if (Boolean.TRUE.equals(request.getSimulateFailure())) {
            throw new IllegalStateException("模拟支付失败，用于验证 Seata 全局回滚");
        }

        Payment existingPayment = getByOrderNo(request.getOrderNo());
        if (existingPayment != null) {
            return existingPayment;
        }

        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setPayNo(generatePayNo(now));
        payment.setOrderNo(request.getOrderNo());
        payment.setUserId(request.getUserId());
        payment.setPayAmount(request.getPayAmount());
        payment.setPayMethod(defaultText(request.getPayMethod(), "支付宝"));
        payment.setPayStatus("支付成功");
        payment.setCallbackContent("Seata AT 模式支付分支执行成功");
        payment.setCallbackTime(now);
        payment.setCreateTime(now);
        payment.setUpdateTime(now);

        int rows = paymentMapper.insert(payment);
        if (rows <= 0) {
            throw new IllegalStateException("支付单创建失败");
        }
        return payment;
    }

    @Override
    public Long countUndoLog() {
        Long count = paymentMapper.countUndoLog();
        return count == null ? 0L : count;
    }

    private void validateRequest(PaymentCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("支付请求不能为空");
        }
        if (request.getOrderNo() == null || request.getOrderNo().isBlank()) {
            throw new IllegalArgumentException("订单编号不能为空");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (request.getPayAmount() == null || request.getPayAmount().signum() <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
    }

    private String generatePayNo(LocalDateTime now) {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "PAY" + PAY_TIME_FORMATTER.format(now) + suffix;
    }

    private String defaultText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
