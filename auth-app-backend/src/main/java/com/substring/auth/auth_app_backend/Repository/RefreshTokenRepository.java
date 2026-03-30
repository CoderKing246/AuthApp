package com.substring.auth.auth_app_backend.Repository;

import com.substring.auth.auth_app_backend.model.RefreshToken;
import com.substring.auth.auth_app_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByJti(String jti);
    List<RefreshToken> findByUser(User user);
}
