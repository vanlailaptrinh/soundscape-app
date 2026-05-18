package com.example.soundscape_app.security;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.auth.Role;
import com.example.soundscape_app.enums.RoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    private final Auth auth;

    public CustomUserDetails(Auth auth) {
        this.auth = auth;
    }

    public Long getId() {
        return auth.getId();
    }

    public Set<RoleEnum> getRoles() {
        return auth.getRoleEntities().stream()
                .map(Role::getName) // Lấy RoleEnum từ entity
                .collect(Collectors.toSet()); // Chuyển thành Set<RoleEnum>
    }

    @Override
    public String getUsername() {
        return auth.getUsername();
    }

    @Override
    public String getPassword() {
        return auth.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // Nếu có roles thì add vào đây
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
