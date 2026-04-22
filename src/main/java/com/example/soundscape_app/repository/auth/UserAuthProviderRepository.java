package com.spotify.repository.auth;

import com.spotify.entity.auth.UserProvider;
import com.spotify.enums.AuthProviderEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserProvider, Long> {
    Optional<UserProvider> findByProviderAndProviderId(AuthProviderEnum provider, String providerId);
}