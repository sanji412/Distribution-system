package com.buu.gateway.dto;

import java.util.List;

/**
 * System monitor page response built from runtime and governance data.
 *
 * @param cards top monitor cards
 * @param rows detailed monitor rows
 * @param knowledge static training knowledge mapping
 */
public record SystemMonitorResponse(
        List<MonitorCard> cards,
        List<MonitorRow> rows,
        List<String> knowledge
) {

    /**
     * Top monitor card.
     *
     * @param title card title
     * @param value card value
     * @param tone frontend visual tone
     */
    public record MonitorCard(String title, String value, String tone) {
    }

    /**
     * Detailed monitor table row.
     *
     * @param metric metric name
     * @param value metric value
     * @param description metric description
     */
    public record MonitorRow(String metric, String value, String description) {
    }
}
