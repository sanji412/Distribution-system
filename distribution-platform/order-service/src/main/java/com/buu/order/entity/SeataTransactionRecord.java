package com.buu.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Seata 全局事务审计记录
 * 长期保存全局事务最终结果，便于前端展示已完成的提交和回滚链路。
 */
@Data
@TableName("seata_transaction_record")
public class SeataTransactionRecord {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;
    private String orderNo;
    private String xid;
    private String transactionStatus;
    private String stockBranchStatus;
    private String payBranchStatus;
    private String failureReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
