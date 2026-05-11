package com.example.soundscape_app.service.auth;

import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.auth.Role;
import com.example.soundscape_app.enums.RoleEnum;
import com.example.soundscape_app.repository.auth.RoleRepository;
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
