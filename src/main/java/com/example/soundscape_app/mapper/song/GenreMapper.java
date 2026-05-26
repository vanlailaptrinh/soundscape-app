package com.example.soundscape_app.mapper.song;

import com.example.soundscape_app.dto.response.song.GenreResponse;
import com.example.soundscape_app.entity.song.Genre;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    GenreResponse toSongResponse(Genre songGenre);
    List<GenreResponse> toResponseList(List<Genre> songGenres);
}
