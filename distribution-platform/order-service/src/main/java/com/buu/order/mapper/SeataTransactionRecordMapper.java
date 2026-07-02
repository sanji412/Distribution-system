package com.buu.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.order.entity.SeataTransactionRecord;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Seata 全局事务审计 Mapper
 * 负责读取订单中心自己的事务审计记录和 undo_log 数量。
 */
public interface SeataTransactionRecordMapper extends BaseMapper<SeataTransactionRecord> {

    /**
     * 查询最近的 Seata 全局事务记录
     *
     * @return 最近 8 条事务审计记录
     */
    @Select("""
            SELECT
                record_id,
                order_no,
                xid,
                transaction_status,
                stock_branch_status,
                pay_branch_status,
                failure_reason,
                create_time,
                update_time
            FROM seata_transaction_record
            ORDER BY create_time DESC
            LIMIT 8
            """)
    List<SeataTransactionRecord> selectRecentRecords();

    /**
     * 查询订单库当前 undo_log 记录数
     *
     * @return undo_log 当前记录数
     */
    @Select("SELECT COUNT(1) FROM undo_log")
    Long countOrderUndoLog();
}
