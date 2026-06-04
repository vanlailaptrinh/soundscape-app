package com.example.soundscape_app.mapper.song;

import org.mapstruct.Mapper;

import com.example.soundscape_app.dto.response.album.AlbumResponse;
import com.example.soundscape_app.entity.album.Album;

@Mapper(componentModel = "spring")
public interface AlbumMapper {
    AlbumResponse toResponse(Album album);
}
