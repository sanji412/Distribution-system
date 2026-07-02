package com.buu.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.buu.order.entity.SeataTransactionRecord;
import com.buu.order.mapper.SeataTransactionRecordMapper;
import com.buu.order.service.SeataTransactionRecordService;
import io.seata.core.context.RootContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Seata 全局事务审计服务实现
 * 审计写入不加入当前全局事务，避免回滚场景下看不到失败记录。
 */
@Service
public class SeataTransactionRecordServiceImpl implements SeataTransactionRecordService {

    private final SeataTransactionRecordMapper recordMapper;

    public SeataTransactionRecordServiceImpl(SeataTransactionRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordStarted(String orderNo, String xid) {
        withoutGlobalTransaction(() -> {
            LocalDateTime now = LocalDateTime.now();
            SeataTransactionRecord record = new SeataTransactionRecord();
            record.setOrderNo(orderNo);
            record.setXid(xid);
            record.setTransactionStatus("RUNNING");
            record.setStockBranchStatus("PENDING");
            record.setPayBranchStatus("PENDING");
            record.setCreateTime(now);
            record.setUpdateTime(now);
            recordMapper.insert(record);
        });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markStockSucceeded(String orderNo, String xid) {
        updateRecord(orderNo, xid, null, "COMMITTED", null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCommitted(String orderNo, String xid) {
        updateRecord(orderNo, xid, "COMMITTED", "COMMITTED", "COMMITTED", null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRolledBack(String orderNo,
                               String xid,
                               String stockBranchStatus,
                               String payBranchStatus,
                               String failureReason) {
        updateRecord(orderNo, xid, "ROLLED_BACK", stockBranchStatus, payBranchStatus, failureReason);
    }

    private void updateRecord(String orderNo,
                              String xid,
                              String transactionStatus,
                              String stockBranchStatus,
                              String payBranchStatus,
                              String failureReason) {
        withoutGlobalTransaction(() -> {
            LambdaUpdateWrapper<SeataTransactionRecord> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SeataTransactionRecord::getOrderNo, orderNo);
            if (xid != null && !xid.isBlank()) {
                wrapper.eq(SeataTransactionRecord::getXid, xid);
            }
            if (transactionStatus != null) {
                wrapper.set(SeataTransactionRecord::getTransactionStatus, transactionStatus);
            }
            if (stockBranchStatus != null) {
                wrapper.set(SeataTransactionRecord::getStockBranchStatus, stockBranchStatus);
            }
            if (payBranchStatus != null) {
                wrapper.set(SeataTransactionRecord::getPayBranchStatus, payBranchStatus);
            }
            if (failureReason != null) {
                wrapper.set(SeataTransactionRecord::getFailureReason, truncate(failureReason, 500));
            }
            wrapper.set(SeataTransactionRecord::getUpdateTime, LocalDateTime.now());
            recordMapper.update(null, wrapper);
        });
    }

    private void withoutGlobalTransaction(Runnable action) {
        String suspendedXid = RootContext.getXID();
        boolean unbound = suspendedXid != null && !suspendedXid.isBlank();
        if (unbound) {
            RootContext.unbind();
        }
        try {
            action.run();
        } finally {
            if (unbound) {
                RootContext.bind(suspendedXid);
            }
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
