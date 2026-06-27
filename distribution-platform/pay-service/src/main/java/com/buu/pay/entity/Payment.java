package com.buu.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单实体
 * 保存支付单号、订单号、支付金额、支付状态和回调信息。
 */
@Data
@TableName("payment")
public class Payment {

    @TableId(value = "pay_id", type = IdType.AUTO)
    private Long payId;
    private String payNo;
    private String orderNo;
    private Long userId;
    private BigDecimal payAmount;
    private String payMethod;
    private String payStatus;
    private String callbackContent;
    private LocalDateTime callbackTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
