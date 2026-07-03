package com.buu.gateway.auth;

public record GatewayUserContext(Long userId, String username, String role) {
}
