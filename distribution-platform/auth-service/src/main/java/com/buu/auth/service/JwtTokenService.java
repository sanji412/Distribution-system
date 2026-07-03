package com.buu.auth.service;

import com.buu.auth.dto.TokenClaims;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class JwtTokenService {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String secret;
    private final String issuer;
    private final long expireMinutes;

    public JwtTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.expire-minutes}") long expireMinutes
    ) {
        this.secret = secret;
        this.issuer = issuer;
        this.expireMinutes = expireMinutes;
    }

    public String generateToken(TokenClaims claims) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + getExpireSeconds();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", claims.getUserId());
        payload.put("username", claims.getUsername());
        payload.put("role", claims.getRole());
        payload.put("iss", issuer);
        payload.put("iat", issuedAt);
        payload.put("exp", expiresAt);

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public TokenClaims parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("无效的登录凭证");
        }

        String[] segments = token.split("\\.");
        if (segments.length != 3) {
            throw new IllegalArgumentException("无效的登录凭证");
        }

        String unsignedToken = segments[0] + "." + segments[1];
        String expectedSignature = sign(unsignedToken);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                segments[2].getBytes(StandardCharsets.UTF_8)
        )) {
            throw new IllegalArgumentException("无效的登录凭证");
        }

        Map<String, Object> payload = decodePayload(segments[1]);
        if (!issuer.equals(payload.get("iss"))) {
            throw new IllegalArgumentException("无效的签发方");
        }
        if (Instant.now().getEpochSecond() >= toLong(payload.get("exp"))) {
            throw new IllegalArgumentException("登录凭证已过期");
        }

        return new TokenClaims(
                toLong(payload.get("sub")),
                Objects.toString(payload.get("username"), ""),
                Objects.toString(payload.get("role"), "")
        );
    }

    public long getExpireSeconds() {
        return expireMinutes * 60;
    }

    private String encodeJson(Map<String, Object> data) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(data));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 序列化失败", exception);
        }
    }

    private Map<String, Object> decodePayload(String payloadSegment) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(payloadSegment);
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("无效的登录凭证", exception);
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 签名失败", exception);
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }
}
