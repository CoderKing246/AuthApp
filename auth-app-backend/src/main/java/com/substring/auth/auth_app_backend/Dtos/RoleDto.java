package com.substring.auth.auth_app_backend.Dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class RoleDto {
    private UUID id;
    private String name;
}
