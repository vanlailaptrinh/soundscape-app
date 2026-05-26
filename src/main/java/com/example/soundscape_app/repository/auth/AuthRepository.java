package com.example.soundscape_app.repository.auth;

import com.example.soundscape_app.entity.auth.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {
    Optional<Auth> findByEmail(String email);
    Optional<Auth> findByUsername(String username);
    Optional<Auth> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
