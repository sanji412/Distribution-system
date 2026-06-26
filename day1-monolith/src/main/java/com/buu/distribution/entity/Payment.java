package com.buu.distribution.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.AUTO)
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
