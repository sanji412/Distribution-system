package com.buu.auth.service;

import com.buu.auth.dto.TokenClaims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private final JwtTokenService tokenService = new JwtTokenService(
            "unit-test-secret-for-jwt",
            "distribution-platform",
            120
    );

    @Test
    void generateTokenCanBeParsedBackToUserClaims() {
        String token = tokenService.generateToken(new TokenClaims(1L, "admin", "admin"));

        TokenClaims claims = tokenService.parseToken(token);

        assertThat(claims.getUserId()).isEqualTo(1L);
        assertThat(claims.getUsername()).isEqualTo("admin");
        assertThat(claims.getRole()).isEqualTo("admin");
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = tokenService.generateToken(new TokenClaims(2L, "user01", "user"));
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> tokenService.parseToken(tampered))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效");
    }
}
