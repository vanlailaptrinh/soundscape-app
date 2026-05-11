package com.example.soundscape_app.repository.auth;

import com.example.soundscape_app.entity.auth.UserProvider;
import com.example.soundscape_app.enums.AuthProviderEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserProvider, Long> {
    Optional<UserProvider> findByProviderAndProviderId(AuthProviderEnum provider, String providerId);
}