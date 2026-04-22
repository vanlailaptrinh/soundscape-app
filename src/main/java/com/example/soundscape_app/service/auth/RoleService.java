package com.spotify.service.auth;

import com.spotify.entity.auth.Auth;
import com.spotify.entity.auth.Role;
import com.spotify.enums.RoleEnum;
import com.spotify.repository.auth.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getRole(RoleEnum roleEnum) {
        return roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public boolean userHasRole(Auth user, RoleEnum roleEnum) {
        return user.getRoleEntities()
                .stream()
                .anyMatch(r -> r.getName().equals(roleEnum));
    }
}
