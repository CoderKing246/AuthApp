package com.substring.auth.auth_app_backend.Dtos;

import lombok.Data;

@Data
public class LoginDto {
    private String email;
    private String password;
}
