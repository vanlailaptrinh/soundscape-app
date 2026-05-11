package com.example.soundscape_app.security;

import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.repository.auth.AuthRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AuthRepository authRepository;

    public CustomUserDetailsService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth auth = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Auth not found with email: " + email));
        return new CustomUserDetails(auth);
    }

    public CustomUserDetails loadUserById(Long id) {
        Auth auth = authRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Auth not found with id: " + id));
        return new CustomUserDetails(auth);
    }
}

