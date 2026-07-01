package com.buu.stock.service.impl;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.buu.stock.config.StockSentinelRuleConfig;
import com.buu.stock.dto.SentinelRuleDTO;
import com.buu.stock.service.SentinelRuleQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reads Sentinel rule definitions and current runtime counters.
 */
@Service
public class SentinelRuleQueryServiceImpl implements SentinelRuleQueryService {

    private final StockSentinelRuleConfig ruleConfig;

    public SentinelRuleQueryServiceImpl(StockSentinelRuleConfig ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    @Override
    public List<SentinelRuleDTO> listRules() {
        FlowRule stockDeductRule = ruleConfig.buildStockDeductFlowRule();
        ParamFlowRule hotParamRule = ruleConfig.buildProductHotParamRule();
        return List.of(flowRuleRow(stockDeductRule), paramRuleRow(hotParamRule));
    }

    private SentinelRuleDTO flowRuleRow(FlowRule rule) {
        long qps = passQps(rule.getResource());
        long blocked = blockQps(rule.getResource());
        return new SentinelRuleDTO(
                rule.getResource(),
                rule.getCount(),
                qps,
                blocked,
                status(qps, blocked, rule.getCount()),
                flowStrategy(rule.getControlBehavior())
        );
    }

    private SentinelRuleDTO paramRuleRow(ParamFlowRule rule) {
        long qps = passQps(rule.getResource());
        long blocked = blockQps(rule.getResource());
        return new SentinelRuleDTO(
                rule.getResource(),
                rule.getCount(),
                qps,
                blocked,
                status(qps, blocked, rule.getCount()),
                "热点参数"
        );
    }

    private String flowStrategy(int controlBehavior) {
        if (controlBehavior == RuleConstant.CONTROL_BEHAVIOR_WARM_UP) {
            return "预热限流";
        }
        if (controlBehavior == RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER) {
            return "匀速排队";
        }
        return "快速失败";
    }

    private String status(long qps, long blocked, double threshold) {
        if (blocked > 0 || qps >= threshold) {
            return "警告";
        }
        return "正常";
    }

    private long passQps(String resource) {
        ClusterNode node = ClusterBuilderSlot.getClusterNode(resource);
        return node == null ? 0L : Math.round(node.passQps());
    }

    private long blockQps(String resource) {
        ClusterNode node = ClusterBuilderSlot.getClusterNode(resource);
        return node == null ? 0L : Math.round(node.blockQps());
    }
}
