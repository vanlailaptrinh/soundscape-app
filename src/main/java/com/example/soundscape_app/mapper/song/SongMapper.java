package com.example.soundscape_app.mapper.song;


import com.example.soundscape_app.dto.response.song.ListSongResponse;
import com.example.soundscape_app.dto.response.song.SongDetailResponse;
import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.entity.song.Genre;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.enums.GenreEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SongMapper {
    @Named("mapGenres")
    default List<GenreEnum> mapGenres(Set<Genre> genres) {
        if (genres == null) {
            return Collections.emptyList();
        }
        return genres.stream()
            .map(Genre::getName)
            .sorted()
            .collect(Collectors.toList());
    }

    @Mapping(target = "genres", source = "genres", qualifiedByName = "mapGenres")
    @Mapping(target = "artistId", source = "auth.id")
    @Mapping(target = "artistName", source = "auth.username")
    SongResponse toSongResponse(Song song);

    @Mapping(target = "artistEmail", source = "auth.email")
    ListSongResponse toListSongResponse(Song song);

    @Mapping(target = "genres", source = "genres", qualifiedByName = "mapGenres")
    @Mapping(target = "authId", source = "auth.id")
    @Mapping(target = "authUsername", source = "auth.username")
    SongDetailResponse toSongDetailResponse(Song song);
}
