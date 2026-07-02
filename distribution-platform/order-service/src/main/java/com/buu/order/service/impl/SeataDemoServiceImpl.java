package com.buu.order.service.impl;

import com.buu.order.client.PayFeignClient;
import com.buu.order.client.StockFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.SeataFlowResponse;
import com.buu.order.entity.SeataTransactionRecord;
import com.buu.order.mapper.SeataTransactionRecordMapper;
import com.buu.order.service.SeataDemoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Seata 演示数据服务实现
 * 汇总真实事务审计记录、undo_log 数量和 TC/RM 可用状态。
 */
@Service
public class SeataDemoServiceImpl implements SeataDemoService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SeataTransactionRecordMapper recordMapper;
    private final StockFeignClient stockFeignClient;
    private final PayFeignClient payFeignClient;

    @Value("${seata.service.grouplist.default:127.0.0.1:8091}")
    private String seataServerAddr;

    public SeataDemoServiceImpl(SeataTransactionRecordMapper recordMapper,
                                StockFeignClient stockFeignClient,
                                PayFeignClient payFeignClient) {
        this.recordMapper = recordMapper;
        this.stockFeignClient = stockFeignClient;
        this.payFeignClient = payFeignClient;
    }

    @Override
    public SeataFlowResponse getFlow() {
        List<SeataFlowResponse.UndoLogMetric> undoLogs = buildUndoLogMetrics();
        boolean stockAvailable = isAvailable(undoLogs, "stock_db");
        boolean payAvailable = isAvailable(undoLogs, "pay_db");
        boolean tcAvailable = isSeataServerAvailable();
        List<SeataTransactionRecord> records = recordMapper.selectRecentRecords();
        return new SeataFlowResponse(
                buildSteps(undoLogs, stockAvailable, payAvailable),
                buildMetrics(tcAvailable, stockAvailable, payAvailable),
                undoLogs,
                buildRecords(records),
                buildLogs(records)
        );
    }

    private List<SeataFlowResponse.Step> buildSteps(List<SeataFlowResponse.UndoLogMetric> undoLogs,
                                                    boolean stockAvailable,
                                                    boolean payAvailable) {
        return List.of(
                new SeataFlowResponse.Step("订单服务", "TM 在线", "service"),
                new SeataFlowResponse.Step("@GlobalTransactional", "开启全局事务", "success"),
                new SeataFlowResponse.Step("库存服务", stockAvailable ? "RM 在线" : "RM 不可用", stockAvailable ? "service" : "warning"),
                new SeataFlowResponse.Step("扣减库存", "undo_log " + countOf(undoLogs, "stock_db"), "warning"),
                new SeataFlowResponse.Step("支付服务", payAvailable ? "RM 在线" : "RM 不可用", payAvailable ? "service" : "warning"),
                new SeataFlowResponse.Step("创建支付单", "undo_log " + countOf(undoLogs, "pay_db"), "success")
        );
    }

    private List<SeataFlowResponse.Metric> buildMetrics(boolean tcAvailable,
                                                        boolean stockAvailable,
                                                        boolean payAvailable) {
        return List.of(
                new SeataFlowResponse.Metric("Seata TC", tcAvailable ? "在线" : "离线", seataServerAddr, tcAvailable ? "success" : "danger"),
                new SeataFlowResponse.Metric("订单服务 TM", "在线", "order-center:8005", "success"),
                new SeataFlowResponse.Metric("库存服务 RM", stockAvailable ? "在线" : "离线", "stock-center:8003", stockAvailable ? "success" : "danger"),
                new SeataFlowResponse.Metric("支付服务 RM", payAvailable ? "在线" : "离线", "pay-center:8006", payAvailable ? "success" : "danger")
        );
    }

    private List<SeataFlowResponse.UndoLogMetric> buildUndoLogMetrics() {
        return List.of(
                new SeataFlowResponse.UndoLogMetric("order_db", "order-center", safeOrderUndoLogCount(), "可读取"),
                undoLogMetric("stock_db", "stock-center", () -> stockFeignClient.countUndoLog()),
                undoLogMetric("pay_db", "pay-center", () -> payFeignClient.countUndoLog())
        );
    }

    private SeataFlowResponse.UndoLogMetric undoLogMetric(String databaseName,
                                                          String serviceName,
                                                          Supplier<R<Long>> supplier) {
        try {
            R<Long> response = supplier.get();
            if (response != null && Integer.valueOf(200).equals(response.getCode()) && response.getData() != null) {
                return new SeataFlowResponse.UndoLogMetric(databaseName, serviceName, response.getData(), "可读取");
            }
            return new SeataFlowResponse.UndoLogMetric(databaseName, serviceName, 0L, "服务不可用");
        } catch (RuntimeException exception) {
            return new SeataFlowResponse.UndoLogMetric(databaseName, serviceName, 0L, "服务不可用");
        }
    }

    private Long safeOrderUndoLogCount() {
        Long count = recordMapper.countOrderUndoLog();
        return count == null ? 0L : count;
    }

    private List<SeataFlowResponse.TransactionRecord> buildRecords(List<SeataTransactionRecord> records) {
        List<SeataFlowResponse.TransactionRecord> rows = new ArrayList<>();
        for (SeataTransactionRecord record : records) {
            rows.add(new SeataFlowResponse.TransactionRecord(
                    record.getOrderNo(),
                    defaultText(record.getXid(), "-"),
                    record.getTransactionStatus(),
                    record.getStockBranchStatus(),
                    record.getPayBranchStatus(),
                    defaultText(record.getFailureReason(), "-"),
                    record.getCreateTime() == null ? "-" : TIME_FORMATTER.format(record.getCreateTime())
            ));
        }
        return rows;
    }

    private List<SeataFlowResponse.LogLine> buildLogs(List<SeataTransactionRecord> records) {
        List<SeataFlowResponse.LogLine> logs = new ArrayList<>();
        for (SeataTransactionRecord record : records) {
            if ("COMMITTED".equals(record.getTransactionStatus())) {
                logs.add(new SeataFlowResponse.LogLine(
                        "success",
                        record.getOrderNo() + "：xid=" + defaultText(record.getXid(), "-")
                                + " → 库存分支 " + record.getStockBranchStatus()
                                + " → 支付分支 " + record.getPayBranchStatus()
                                + " → 全局事务提交"
                ));
            } else if ("ROLLED_BACK".equals(record.getTransactionStatus())) {
                logs.add(new SeataFlowResponse.LogLine(
                        "danger",
                        record.getOrderNo() + "：xid=" + defaultText(record.getXid(), "-")
                                + " → 库存分支 " + record.getStockBranchStatus()
                                + " → 支付分支 " + record.getPayBranchStatus()
                                + " → 全局事务回滚"
                                + " → " + defaultText(record.getFailureReason(), "失败原因未记录")
                ));
            } else {
                logs.add(new SeataFlowResponse.LogLine(
                        "warning",
                        record.getOrderNo() + "：xid=" + defaultText(record.getXid(), "-") + " → 全局事务执行中"
                ));
            }
        }
        if (logs.isEmpty()) {
            logs.add(new SeataFlowResponse.LogLine(
                    "warning",
                    "暂无 Seata 事务审计记录，可通过 /api/order/create 发起 Seata 事务演示"
            ));
        }
        return logs;
    }

    private boolean isSeataServerAvailable() {
        HostAndPort hostAndPort = parseHostAndPort(seataServerAddr);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostAndPort.host(), hostAndPort.port()), 500);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private HostAndPort parseHostAndPort(String address) {
        String value = defaultText(address, "127.0.0.1:8091");
        String[] parts = value.split(":");
        if (parts.length != 2) {
            return new HostAndPort("127.0.0.1", 8091);
        }
        try {
            return new HostAndPort(parts[0], Integer.parseInt(parts[1]));
        } catch (NumberFormatException exception) {
            return new HostAndPort("127.0.0.1", 8091);
        }
    }

    private Long countOf(List<SeataFlowResponse.UndoLogMetric> undoLogs, String databaseName) {
        return undoLogs.stream()
                .filter(metric -> databaseName.equals(metric.getDatabaseName()))
                .findFirst()
                .map(SeataFlowResponse.UndoLogMetric::getCount)
                .orElse(0L);
    }

    private boolean isAvailable(List<SeataFlowResponse.UndoLogMetric> undoLogs, String databaseName) {
        return undoLogs.stream()
                .filter(metric -> databaseName.equals(metric.getDatabaseName()))
                .findFirst()
                .map(metric -> "可读取".equals(metric.getStatus()))
                .orElse(false);
    }

    private String defaultText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private record HostAndPort(String host, int port) {
    }
}
