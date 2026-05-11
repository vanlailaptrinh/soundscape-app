package com.example.soundscape_app.repository.auth;

import com.example.soundscape_app.entity.auth.Role;
import com.example.soundscape_app.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}
