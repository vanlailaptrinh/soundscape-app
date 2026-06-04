package com.example.soundscape_app.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.soundscape_app.entity.auth.Auth;

public interface UserRepository extends JpaRepository<Auth, Long> {
    Optional<Auth> findByEmail(String email);

}
