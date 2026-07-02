package com.buu.pay.service;

import com.buu.pay.dto.PaymentCreateRequest;
import com.buu.pay.entity.Payment;
import com.buu.pay.mapper.PaymentMapper;
import com.buu.pay.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentMapper paymentMapper;

    @Test
    void createPaymentPersistsSuccessfulPaymentRecord() {
        given(paymentMapper.insert(any(Payment.class))).willAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPayId(66L);
            return 1;
        });

        PaymentService paymentService = new PaymentServiceImpl(paymentMapper);
        Payment payment = paymentService.createPayment(buildRequest(false));

        assertThat(payment.getPayId()).isEqualTo(66L);
        assertThat(payment.getPayNo()).startsWith("PAY");
        assertThat(payment.getOrderNo()).isEqualTo("DD202607020001");
        assertThat(payment.getPayStatus()).isEqualTo("支付成功");

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentMapper).insert(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getPayAmount()).isEqualByComparingTo("736.00");
        assertThat(paymentCaptor.getValue().getCallbackContent()).contains("Seata");
    }

    @Test
    void createPaymentThrowsBeforeInsertWhenSimulationFailureIsRequested() {
        PaymentService paymentService = new PaymentServiceImpl(paymentMapper);

        assertThatThrownBy(() -> paymentService.createPayment(buildRequest(true)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("模拟支付失败");

        verify(paymentMapper, never()).insert(any(Payment.class));
    }

    private PaymentCreateRequest buildRequest(boolean simulateFailure) {
        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setOrderNo("DD202607020001");
        request.setUserId(1L);
        request.setPayAmount(new BigDecimal("736.00"));
        request.setPayMethod("支付宝");
        request.setSimulateFailure(simulateFailure);
        return request;
    }
}
