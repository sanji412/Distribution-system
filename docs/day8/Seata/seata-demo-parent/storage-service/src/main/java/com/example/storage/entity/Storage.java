package com.example.storage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 库存实体类
 * 对应数据库表 t_storage
 */
@Data
@TableName("t_storage")
public class Storage {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 总库存数量
     */
    private Integer total;

    /**
     * 已使用库存数量
     */
    private Integer used;
}