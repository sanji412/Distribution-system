package com.buu.stock.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StockSentinelRuleConfigTest {

    @Test
    void stockDeductFlowRuleLimitsQpsToOneHundred() {
        StockSentinelRuleConfig config = new StockSentinelRuleConfig();

        FlowRule rule = config.buildStockDeductFlowRule();

        assertThat(rule.getResource()).isEqualTo("stockDeduct");
        assertThat(rule.getGrade()).isEqualTo(RuleConstant.FLOW_GRADE_QPS);
        assertThat(rule.getCount()).isEqualTo(100D);
    }

    @Test
    void stockByProductHotParamRuleUsesProductIdParameter() {
        StockSentinelRuleConfig config = new StockSentinelRuleConfig();

        ParamFlowRule rule = config.buildProductHotParamRule();

        assertThat(rule.getResource()).isEqualTo("stockByProduct");
        assertThat(rule.getGrade()).isEqualTo(RuleConstant.FLOW_GRADE_QPS);
        assertThat(rule.getParamIdx()).isZero();
        assertThat(rule.getCount()).isEqualTo(20D);
    }
}
