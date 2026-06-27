package com.buu.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库实体
 * 保存仓库名称、地址和启用状态，支撑多仓库库存管理。
 */
@Data
@TableName("warehouse")
public class Warehouse {

    @TableId(value = "warehouse_id", type = IdType.AUTO)
    private Long warehouseId;
    private String warehouseName;
    private String warehouseAddress;
    private Integer status;
    private LocalDateTime createTime;
}
