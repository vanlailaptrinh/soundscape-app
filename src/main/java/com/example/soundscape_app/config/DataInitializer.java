package com.spotify.config;

import com.spotify.entity.auth.Role;
import com.spotify.entity.song.Genre;
import com.spotify.enums.GenreEnum;
import com.spotify.enums.RoleEnum;
import com.spotify.repository.auth.RoleRepository;
import com.spotify.repository.song.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final SongGenreRepository songGenreRepository;


    @Override
    public void run(String... args) {
        // Initialize roles
        List<Role> existingRoleEntities = roleRepository.findAll();
        Set<RoleEnum> existingRoleNames = existingRoleEntities.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        List<Role> rolesToCreate = Arrays.stream(RoleEnum.values())
                .filter(roleEnum -> !existingRoleNames.contains(roleEnum))
                .map(roleEnum -> {
                    Role role = new Role();
                    role.setName(roleEnum);
                    return role;
                })
                .collect(Collectors.toList());

        if (!rolesToCreate.isEmpty()) {
            roleRepository.saveAll(rolesToCreate);
            rolesToCreate.forEach(role -> System.out.println("Created role: " + role.getName()));
        }


        List<Genre> existingGenres = songGenreRepository.findAll();
        Set<GenreEnum> existingGenreNames = existingGenres.stream()
                .map(Genre::getName)
                .collect(Collectors.toSet());

        List<Genre> genresToCreate = Arrays.stream(GenreEnum.values())
                .filter(genreEnum -> !existingGenreNames.contains(genreEnum))
                .map(genreEnum -> {
                    Genre genre = new Genre();
                    genre.setName(genreEnum);
                    return genre;
                })
                .collect(Collectors.toList());

        if (!genresToCreate.isEmpty()) {
            songGenreRepository.saveAll(genresToCreate);
            genresToCreate.forEach(genre -> System.out.println("Created genre: " + genre.getName()));
        }
//
//        String adminEmail = "admin@gmail.com";
//
//        if (userRepository.findByEmail(adminEmail).isEmpty()) {
//            roleRepository.findByName(RoleEnum.ADMIN)
//                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
//
//            String rawPassword = "vudeptrai";
//            String hashedPassword = passwordEncoder.encode(rawPassword);
//
//            List<RoleEnum> roleEnums = Arrays.asList(RoleEnum.ADMIN);
//            authService.createUserWithHashedPassword(adminEmail, hashedPassword, roleEnums);
//
//            System.out.println("Default admin auth created:");
//            System.out.println("Email: " + adminEmail);
//            System.out.println("Password: " + rawPassword + " (please change after first login)");
//        }
    }
}