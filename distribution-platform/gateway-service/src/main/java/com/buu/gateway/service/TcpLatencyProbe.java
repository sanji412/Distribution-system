package com.buu.gateway.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TCP based latency probe for registered Nacos service instances.
 */
@Component
public class TcpLatencyProbe implements LatencyProbe {

    private static final int CONNECT_TIMEOUT_MS = 800;

    @Override
    public Long probe(String host, int port) {
        long startNanos = System.nanoTime();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            return Math.max(1L, (System.nanoTime() - startNanos) / 1_000_000L);
        } catch (IOException ex) {
            return null;
        }
    }
}
