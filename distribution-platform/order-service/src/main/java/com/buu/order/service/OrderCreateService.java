package com.buu.order.service;

import com.buu.order.dto.OrderCreateRequest;
import com.buu.order.dto.OrderCreateResponse;

/**
 * 订单创建服务
 * 负责组织订单、库存和支付三个服务的 Seata 全局事务。
 */
public interface OrderCreateService {

    /**
     * 创建订单并完成库存扣减、支付单创建。
     *
     * @param request 创建订单请求
     * @return 创建结果
     */
    OrderCreateResponse createOrder(OrderCreateRequest request);
}
