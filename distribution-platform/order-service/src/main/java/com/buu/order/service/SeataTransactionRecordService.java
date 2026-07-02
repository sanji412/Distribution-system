package com.buu.order.service;

/**
 * Seata 全局事务审计服务
 * 记录订单创建链路中 TM/RM 的执行结果。
 */
public interface SeataTransactionRecordService {

    void recordStarted(String orderNo, String xid);

    void markStockSucceeded(String orderNo, String xid);

    void markCommitted(String orderNo, String xid);

    void markRolledBack(String orderNo,
                        String xid,
                        String stockBranchStatus,
                        String payBranchStatus,
                        String failureReason);
}
