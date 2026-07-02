package com.buu.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.pay.entity.Payment;
import org.apache.ibatis.annotations.Select;

/**
 * 支付单 Mapper
 * 负责支付单表的基础 CRUD 操作。
 */
public interface PaymentMapper extends BaseMapper<Payment> {

    /**
     * 查询支付库当前 undo_log 记录数
     *
     * @return undo_log 当前记录数
     */
    @Select("SELECT COUNT(1) FROM undo_log")
    Long countUndoLog();
}
