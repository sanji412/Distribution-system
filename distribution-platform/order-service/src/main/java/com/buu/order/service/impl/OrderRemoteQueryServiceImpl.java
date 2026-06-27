package com.buu.order.service.impl;

import com.buu.order.client.PayFeignClient;
import com.buu.order.client.ProductFeignClient;
import com.buu.order.client.StockFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.OrderRemoteDetailDTO;
import com.buu.order.dto.RemotePaymentDTO;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.dto.RemoteStockDTO;
import com.buu.order.entity.OrderItem;
import com.buu.order.entity.OrderMain;
import com.buu.order.service.OrderRemoteLocalQuery;
import com.buu.order.service.OrderRemoteQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单跨服务查询服务实现
 * 订单服务作为消费者，通过 OpenFeign 按 Nacos 服务名调用下游服务。
 */
@Service
public class OrderRemoteQueryServiceImpl implements OrderRemoteQueryService {

    private final OrderRemoteLocalQuery orderRemoteLocalQuery;
    private final ProductFeignClient productFeignClient;
    private final StockFeignClient stockFeignClient;
    private final PayFeignClient payFeignClient;

    public OrderRemoteQueryServiceImpl(OrderRemoteLocalQuery orderRemoteLocalQuery,
                                       ProductFeignClient productFeignClient,
                                       StockFeignClient stockFeignClient,
                                       PayFeignClient payFeignClient) {
        this.orderRemoteLocalQuery = orderRemoteLocalQuery;
        this.productFeignClient = productFeignClient;
        this.stockFeignClient = stockFeignClient;
        this.payFeignClient = payFeignClient;
    }

    /**
     * 查询订单跨服务详情
     *
     * @param orderNo 订单编号
     * @return 聚合后的订单跨服务详情
     */
    @Override
    public OrderRemoteDetailDTO getRemoteDetail(String orderNo) {
        OrderMain order = orderRemoteLocalQuery.getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在：" + orderNo);
        }

        OrderItem item = orderRemoteLocalQuery.getFirstItemByOrderId(order.getOrderId());
        if (item == null) {
            throw new IllegalStateException("订单明细不存在：" + orderNo);
        }

        RemoteProductDTO product = requireData(productFeignClient.detail(item.getProductId()), "product-center");
        List<RemoteStockDTO> stocks = requireData(stockFeignClient.listByProductId(item.getProductId()), "stock-center");
        RemotePaymentDTO payment = requireData(payFeignClient.detailByOrderNo(orderNo), "pay-center");

        return new OrderRemoteDetailDTO(
                order.getOrderNo(),
                order.getProductName(),
                order.getProductNum(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                "product-center",
                "stock-center",
                "pay-center",
                product,
                stocks,
                payment
        );
    }

    private <T> T requireData(R<T> response, String serviceName) {
        if (response == null || response.getCode() == null || response.getCode() != 200) {
            throw new IllegalStateException(serviceName + " 调用失败");
        }
        return response.getData();
    }
}
