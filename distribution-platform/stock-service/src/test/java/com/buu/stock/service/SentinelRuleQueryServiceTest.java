package com.buu.stock.service;

import com.buu.stock.config.StockSentinelRuleConfig;
import com.buu.stock.dto.SentinelRuleDTO;
import com.buu.stock.service.impl.SentinelRuleQueryServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SentinelRuleQueryServiceTest {

    @Test
    void returnsConfiguredStockSentinelRulesAsDisplayRows() {
        SentinelRuleQueryService service = new SentinelRuleQueryServiceImpl(new StockSentinelRuleConfig());

        List<SentinelRuleDTO> rules = service.listRules();

        assertThat(rules).extracting(SentinelRuleDTO::getResource)
                .contains("stockDeduct", "stockByProduct");
        assertThat(rules).filteredOn(rule -> rule.getResource().equals("stockDeduct"))
                .singleElement()
                .satisfies(rule -> {
                    assertThat(rule.getThreshold()).isEqualTo(100D);
                    assertThat(rule.getStrategy()).isEqualTo("快速失败");
                    assertThat(rule.getStatus()).isEqualTo("正常");
                });
        assertThat(rules).filteredOn(rule -> rule.getResource().equals("stockByProduct"))
                .singleElement()
                .satisfies(rule -> {
                    assertThat(rule.getThreshold()).isEqualTo(20D);
                    assertThat(rule.getStrategy()).isEqualTo("热点参数");
                });
    }
}
