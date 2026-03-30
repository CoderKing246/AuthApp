package com.substring.auth.auth_app_backend.Dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.substring.auth.auth_app_backend.model.Provider;
import com.substring.auth.auth_app_backend.model.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private String image;
    private String password;
    private LocalDate dateOfBirth;
    private Provider provider;
    private Set<Role> Roles = new HashSet<>();


}
