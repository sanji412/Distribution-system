package com.buu.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.order.dto.OrderListItemDTO;
import com.buu.order.dto.OrderStatusDistributionDTO;
import com.buu.order.dto.OrderSummaryDTO;
import com.buu.order.dto.OrderTrendDTO;
import com.buu.order.entity.OrderMain;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单主表 Mapper
 * 提供订单履约页面所需的汇总统计和列表查询。
 */
public interface OrderMainMapper extends BaseMapper<OrderMain> {

    /**
     * 查询今日订单履约汇总
     *
     * @param startTime 当日开始时间
     * @param endTime 次日开始时间
     * @return 今日订单汇总数据
     */
    OrderSummaryDTO selectTodaySummary(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定日期内的订单状态分布
     *
     * @param startTime 当日开始时间
     * @param endTime 次日开始时间
     * @return 状态分布明细
     */
    List<OrderStatusDistributionDTO> selectStatusDistribution(@Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近七天订单趋势
     *
     * @param startTime 七天窗口开始时间
     * @param endTime 次日开始时间
     * @return 按日期聚合的订单趋势
     */
    List<OrderTrendDTO> selectSevenDayTrend(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据订单编号查询订单主表
     *
     * @param orderNo 订单编号
     * @return 订单主表记录
     */
    OrderMain selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询订单履约列表
     *
     * @return 订单列表行数据
     */
    List<OrderListItemDTO> selectOrderList();

    /**
     * 查询最近的 Seata 事务演示订单
     *
     * @return 最近订单列表行数据
     */
    List<OrderListItemDTO> selectRecentSeataOrderList();
}
