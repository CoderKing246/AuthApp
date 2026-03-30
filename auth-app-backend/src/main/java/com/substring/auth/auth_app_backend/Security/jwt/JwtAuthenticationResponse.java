package com.substring.auth.auth_app_backend.Security.jwt;

import com.substring.auth.auth_app_backend.Dtos.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private UserDto user;
}
