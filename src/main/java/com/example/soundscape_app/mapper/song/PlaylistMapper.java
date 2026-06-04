package com.example.soundscape_app.mapper.song;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.soundscape_app.dto.response.song.PlaylistResponse;
import com.example.soundscape_app.entity.playlist.Playlist;

@Mapper(componentModel = "spring")
public interface PlaylistMapper {

    @Mapping(expression = "java(playlist.getPlayListItem().size())", target = "songCount")
    PlaylistResponse toResponse(Playlist playlist);

}
