package com.buu.order.client;

import com.buu.order.common.R;
import com.buu.order.dto.RemotePaymentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 支付中心 Feign 客户端
 * 通过 Nacos 服务名调用 pay-center 的支付单查询接口。
 */
@FeignClient(name = "pay-center")
public interface PayFeignClient {

    /**
     * 根据订单号查询支付单
     *
     * @param orderNo 订单编号
     * @return 支付中心返回的支付单信息
     */
    @GetMapping("/api/pay/order/{orderNo}")
    R<RemotePaymentDTO> detailByOrderNo(@PathVariable("orderNo") String orderNo);
}
