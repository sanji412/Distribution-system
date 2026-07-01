package com.buu.gateway.service;

/**
 * Measures whether a service instance endpoint is reachable and how long it takes.
 */
@FunctionalInterface
public interface LatencyProbe {

    /**
     * Probes a host and port.
     *
     * @param host target host
     * @param port target port
     * @return latency in milliseconds, or {@code null} when the endpoint is unreachable
     */
    Long probe(String host, int port);
}
