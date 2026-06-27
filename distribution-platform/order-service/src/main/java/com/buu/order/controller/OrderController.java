package com.buu.order.controller;

import com.buu.order.common.R;
import com.buu.order.dto.OrderDashboardResponse;
import com.buu.order.dto.OrderListItemDTO;
import com.buu.order.dto.OrderRemoteDetailDTO;
import com.buu.order.dto.OrderSummaryDTO;
import com.buu.order.service.OrderQueryService;
import com.buu.order.service.OrderRemoteQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单履约 Controller
 * 处理订单履约页面的指标汇总和订单列表查询请求。
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderQueryService orderQueryService;
    private final OrderRemoteQueryService orderRemoteQueryService;

    public OrderController(OrderQueryService orderQueryService, OrderRemoteQueryService orderRemoteQueryService) {
        this.orderQueryService = orderQueryService;
        this.orderRemoteQueryService = orderRemoteQueryService;
    }

    /**
     * 查询订单履约看板数据
     *
     * @return 汇总指标和订单列表
     */
    @GetMapping("/dashboard")
    public R<OrderDashboardResponse> dashboard() {
        return R.success(orderQueryService.getDashboard(LocalDate.now()));
    }

    /**
     * 查询订单履约汇总指标
     *
     * @return 今日订单、成交额、待发货和异常订单数据
     */
    @GetMapping("/summary")
    public R<OrderSummaryDTO> summary() {
        return R.success(orderQueryService.getSummary(LocalDate.now()));
    }

    /**
     * 查询订单履约列表
     *
     * @return 订单列表
     */
    @GetMapping("/list")
    public R<List<OrderListItemDTO>> list() {
        return R.success(orderQueryService.listOrders());
    }

    /**
     * 查询订单跨服务详情
     *
     * @param orderNo 订单编号
     * @return 订单、商品、库存和支付信息的聚合结果
     */
    @GetMapping("/remote-detail/{orderNo}")
    public R<OrderRemoteDetailDTO> remoteDetail(@PathVariable String orderNo) {
        return R.success(orderRemoteQueryService.getRemoteDetail(orderNo));
    }
}
