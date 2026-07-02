package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Seata 事务链路展示响应
 * 为前端订单履约页提供真实订单状态生成的事务链路说明。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeataFlowResponse {

    private List<Step> steps;
    private List<Metric> metrics;
    private List<UndoLogMetric> undoLogs;
    private List<TransactionRecord> records;
    private List<LogLine> logs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Step {
        private String name;
        private String desc;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metric {
        private String name;
        private String value;
        private String desc;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UndoLogMetric {
        private String databaseName;
        private String serviceName;
        private Long count;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionRecord {
        private String orderNo;
        private String xid;
        private String transactionStatus;
        private String stockBranchStatus;
        private String payBranchStatus;
        private String failureReason;
        private String createTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogLine {
        private String type;
        private String text;
    }
}
