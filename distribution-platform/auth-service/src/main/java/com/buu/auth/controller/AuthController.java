package com.buu.auth.controller;

import com.buu.auth.common.R;
import com.buu.auth.dto.LoginRequest;
import com.buu.auth.dto.LoginResponse;
import com.buu.auth.dto.TokenClaims;
import com.buu.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginResponse> login(@RequestBody LoginRequest request) {
        return R.success(authService.login(request));
    }

    @GetMapping("/validate")
    public R<TokenClaims> validate(@RequestHeader("Authorization") String authorization) {
        return R.success(authService.validate(authorization));
    }
}
