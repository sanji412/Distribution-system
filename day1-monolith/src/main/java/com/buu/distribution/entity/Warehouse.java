package com.buu.distribution.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("warehouse")
public class Warehouse {

    @TableId(type = IdType.AUTO)
    private Long warehouseId;
    private String warehouseName;
    private String warehouseAddress;
    private Integer status;
    private LocalDateTime createTime;
}
