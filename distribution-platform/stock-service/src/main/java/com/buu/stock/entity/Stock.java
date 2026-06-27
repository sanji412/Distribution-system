package com.buu.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存实体
 * 保存商品在指定仓库的可用库存数量。
 */
@Data
@TableName("stock")
public class Stock {

    @TableId(value = "stock_id", type = IdType.AUTO)
    private Long stockId;
    private Long productId;
    private Long warehouseId;
    private Integer stockNum;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
