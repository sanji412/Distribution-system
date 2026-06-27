package com.buu.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.order.dto.OrderListItemDTO;
import com.buu.order.dto.OrderSummaryDTO;
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
     * 查询订单履约列表
     *
     * @return 订单列表行数据
     */
    List<OrderListItemDTO> selectOrderList();
}
