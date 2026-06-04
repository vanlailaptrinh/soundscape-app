package com.example.soundscape_app.mapper.song;

import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.dto.response.user.ListUserResponse;
import com.example.soundscape_app.dto.response.user.UserDetailResponse;
import com.example.soundscape_app.dto.response.user.UserResponse;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.auth.Role;
import com.example.soundscape_app.enums.RoleEnum;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(Auth user);

    ArtistResponse toArtistResponse(Auth auth);

    @Mapping(source = "roleEntities", target = "roles", qualifiedByName = "mapRoles")
    ListUserResponse toListUserResponse(Auth auth);

    @Named("mapRoles")
    default Set<RoleEnum> mapRoles(Set<Role> roles) {
        if (roles == null) return null;

        List<RoleEnum> order = List.of(RoleEnum.ADMIN, RoleEnum.ARTIST, RoleEnum.USER);
        return roles.stream()
                .map(Role::getName)
                .sorted(Comparator.comparingInt(order::indexOf))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Mapping(source = "user.roleEntities", target = "role", qualifiedByName = "mapRoles")
    UserDetailResponse toUserDetailResponse(
            Auth user,
            long totalSongs,
            long totalListeningCount,
            Long averageMonthlyListeners
    );
}

