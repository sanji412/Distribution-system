package com.buu.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.auth.dto.LoginRequest;
import com.buu.auth.dto.LoginResponse;
import com.buu.auth.dto.LoginUser;
import com.buu.auth.dto.TokenClaims;
import com.buu.auth.entity.AuthAccount;
import com.buu.auth.exception.AuthException;
import com.buu.auth.mapper.AuthAccountMapper;
import com.buu.auth.service.AuthService;
import com.buu.auth.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthAccountMapper authAccountMapper;
    private final JwtTokenService jwtTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new AuthException("账号或密码不能为空");
        }

        String username = request.getUsername().trim();
        AuthAccount account = authAccountMapper.selectOne(new LambdaQueryWrapper<AuthAccount>()
                .eq(AuthAccount::getUsername, username));
        if (account == null || account.getStatus() == null || account.getStatus() != 1
                || !passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new AuthException("账号或密码错误");
        }

        TokenClaims claims = new TokenClaims(account.getAccountId(), account.getUsername(), account.getRole());
        LoginUser user = new LoginUser(
                account.getAccountId(),
                account.getUsername(),
                account.getNickname(),
                account.getRole()
        );

        return new LoginResponse(
                jwtTokenService.generateToken(claims),
                "Bearer",
                jwtTokenService.getExpireSeconds(),
                user
        );
    }

    @Override
    public TokenClaims validate(String authorization) {
        String token = extractBearerToken(authorization);
        try {
            return jwtTokenService.parseToken(token);
        } catch (IllegalArgumentException exception) {
            throw new AuthException(exception.getMessage());
        }
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AuthException("登录凭证格式错误");
        }
        return authorization.substring("Bearer ".length()).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
