package com.buu.distribution.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock")
public class Stock {

    @TableId(type = IdType.AUTO)
    private Long stockId;
    private Long productId;
    private Long warehouseId;
    private Integer stockNum;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
