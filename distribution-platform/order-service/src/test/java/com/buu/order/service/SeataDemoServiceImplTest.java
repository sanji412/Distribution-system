package com.buu.order.service;

import com.buu.order.dto.OrderListItemDTO;
import com.buu.order.dto.SeataFlowResponse;
import com.buu.order.entity.SeataTransactionRecord;
import com.buu.order.mapper.SeataTransactionRecordMapper;
import com.buu.order.client.PayFeignClient;
import com.buu.order.client.StockFeignClient;
import com.buu.order.common.R;
import com.buu.order.service.impl.SeataDemoServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SeataDemoServiceImplTest {

    @Test
    void getFlowReturnsRealTransactionRecordsAndUndoLogMetrics() {
        SeataTransactionRecordMapper recordMapper = mock(SeataTransactionRecordMapper.class);
        StockFeignClient stockFeignClient = mock(StockFeignClient.class);
        PayFeignClient payFeignClient = mock(PayFeignClient.class);
        given(recordMapper.countOrderUndoLog()).willReturn(0L);
        given(stockFeignClient.countUndoLog()).willReturn(R.success(1L));
        given(payFeignClient.countUndoLog()).willReturn(R.success(0L));
        given(recordMapper.selectRecentRecords()).willReturn(List.of(
                buildRecord("DD202607020001", "xid-commit", "COMMITTED", "COMMITTED", "COMMITTED", null),
                buildRecord("DD202607020002", "xid-rollback", "ROLLED_BACK", "ROLLED_BACK", "SKIPPED", "库存不足")
        ));

        SeataDemoService service = new SeataDemoServiceImpl(recordMapper, stockFeignClient, payFeignClient);
        SeataFlowResponse response = service.getFlow();

        assertThat(response.getUndoLogs()).extracting(SeataFlowResponse.UndoLogMetric::getDatabaseName)
                .containsExactly("order_db", "stock_db", "pay_db");
        assertThat(response.getUndoLogs()).extracting(SeataFlowResponse.UndoLogMetric::getCount)
                .containsExactly(0L, 1L, 0L);
        assertThat(response.getRecords()).extracting(SeataFlowResponse.TransactionRecord::getOrderNo)
                .containsExactly("DD202607020001", "DD202607020002");
        assertThat(response.getLogs()).extracting(SeataFlowResponse.LogLine::getText)
                .anyMatch(text -> text.contains("xid-commit") && text.contains("全局事务提交"))
                .anyMatch(text -> text.contains("xid-rollback") && text.contains("全局事务回滚"));
    }

    @Test
    void getFlowReturnsHintWhenThereAreNoTransactionOrdersYet() {
        SeataTransactionRecordMapper recordMapper = mock(SeataTransactionRecordMapper.class);
        StockFeignClient stockFeignClient = mock(StockFeignClient.class);
        PayFeignClient payFeignClient = mock(PayFeignClient.class);
        given(recordMapper.countOrderUndoLog()).willReturn(0L);
        given(stockFeignClient.countUndoLog()).willReturn(R.success(0L));
        given(payFeignClient.countUndoLog()).willReturn(R.success(0L));
        given(recordMapper.selectRecentRecords()).willReturn(List.of());

        SeataDemoService service = new SeataDemoServiceImpl(recordMapper, stockFeignClient, payFeignClient);
        SeataFlowResponse response = service.getFlow();

        assertThat(response.getLogs()).singleElement()
                .satisfies(log -> {
                    assertThat(log.getType()).isEqualTo("warning");
                    assertThat(log.getText()).contains("/api/order/create");
                });
    }

    private SeataTransactionRecord buildRecord(String orderNo,
                                               String xid,
                                               String transactionStatus,
                                               String stockBranchStatus,
                                               String payBranchStatus,
                                               String failureReason) {
        SeataTransactionRecord record = new SeataTransactionRecord();
        record.setOrderNo(orderNo);
        record.setXid(xid);
        record.setTransactionStatus(transactionStatus);
        record.setStockBranchStatus(stockBranchStatus);
        record.setPayBranchStatus(payBranchStatus);
        record.setFailureReason(failureReason);
        record.setCreateTime(LocalDateTime.of(2026, 7, 2, 10, 0));
        record.setUpdateTime(LocalDateTime.of(2026, 7, 2, 10, 1));
        return record;
    }
}
