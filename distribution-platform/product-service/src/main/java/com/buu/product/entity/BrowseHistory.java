package com.buu.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 浏览历史实体
 * 记录用户浏览商品行为，为后续 AI 搭配推荐提供数据源。
 */
@Data
@TableName("browse_history")
public class BrowseHistory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private LocalDateTime browseTime;
}
