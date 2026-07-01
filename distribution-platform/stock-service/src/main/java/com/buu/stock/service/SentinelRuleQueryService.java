package com.buu.stock.service;

import com.buu.stock.dto.SentinelRuleDTO;

import java.util.List;

/**
 * Sentinel rule query service.
 */
public interface SentinelRuleQueryService {

    /**
     * Lists currently configured Sentinel rules for the frontend monitor table.
     *
     * @return Sentinel rule rows
     */
    List<SentinelRuleDTO> listRules();
}
