package com.buu.distribution.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_main")
public class OrderMain {

    @TableId(type = IdType.AUTO)
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String productName;
    private Integer productNum;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String logisticsCompany;
    private String logisticsNo;
    private String logisticsStatus;
    private String currentLocation;
    private LocalDateTime expectArriveTime;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime finishTime;
}
