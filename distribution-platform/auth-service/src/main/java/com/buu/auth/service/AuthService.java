package com.buu.auth.service;

import com.buu.auth.dto.LoginRequest;
import com.buu.auth.dto.LoginResponse;
import com.buu.auth.dto.TokenClaims;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    TokenClaims validate(String authorization);
}
