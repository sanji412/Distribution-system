package com.buu.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.stock.entity.Stock;
import org.apache.ibatis.annotations.Select;

/**
 * 库存 Mapper
 * 负责库存表的基础 CRUD 操作。
 */
public interface StockMapper extends BaseMapper<Stock> {

    /**
     * 查询库存库当前 undo_log 记录数
     *
     * @return undo_log 当前记录数
     */
    @Select("SELECT COUNT(1) FROM undo_log")
    Long countUndoLog();
}
