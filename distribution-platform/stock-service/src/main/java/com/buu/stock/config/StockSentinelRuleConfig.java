package com.buu.stock.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 库存服务 Sentinel 规则配置
 * 提供今日实训要求的扣减接口限流和商品 ID 热点参数限流默认规则。
 */
@Configuration
public class StockSentinelRuleConfig {

    private static final Logger log = LoggerFactory.getLogger(StockSentinelRuleConfig.class);

    public static final String STOCK_DEDUCT_RESOURCE = "stockDeduct";
    public static final String STOCK_BY_PRODUCT_RESOURCE = "stockByProduct";
    public static final double STOCK_DEDUCT_QPS_LIMIT = 100D;
    public static final double PRODUCT_HOT_PARAM_QPS_LIMIT = 20D;

    /**
     * 应用启动完成后初始化 Sentinel 规则
     *
     * @param event 应用启动完成事件
     */
    @EventListener
    public void initRules(ApplicationReadyEvent event) {
        FlowRuleManager.loadRules(List.of(buildStockDeductFlowRule()));
        ParamFlowRuleManager.loadRules(List.of(buildProductHotParamRule()));
        log.info("Stock Sentinel rules loaded: {} QPS={}, {} hot-param QPS={}",
                STOCK_DEDUCT_RESOURCE,
                STOCK_DEDUCT_QPS_LIMIT,
                STOCK_BY_PRODUCT_RESOURCE,
                PRODUCT_HOT_PARAM_QPS_LIMIT);
    }

    /**
     * 构建库存扣减 QPS 限流规则
     *
     * @return 库存扣减限流规则
     */
    public FlowRule buildStockDeductFlowRule() {
        FlowRule rule = new FlowRule();
        rule.setResource(STOCK_DEDUCT_RESOURCE);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(STOCK_DEDUCT_QPS_LIMIT);
        return rule;
    }

    /**
     * 构建商品 ID 维度热点参数限流规则
     *
     * @return 商品库存热点参数规则
     */
    public ParamFlowRule buildProductHotParamRule() {
        ParamFlowRule rule = new ParamFlowRule(STOCK_BY_PRODUCT_RESOURCE);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setParamIdx(0);
        rule.setCount(PRODUCT_HOT_PARAM_QPS_LIMIT);
        return rule;
    }
}
