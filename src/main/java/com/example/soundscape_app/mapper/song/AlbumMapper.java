package com.example.soundscape_app.mapper.song;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.soundscape_app.dto.response.album.AlbumResponse;
import com.example.soundscape_app.entity.album.Album;
import com.example.soundscape_app.util.S3Util;

@Mapper(componentModel = "spring", uses = {S3Util.class})
public interface AlbumMapper {
    @Mapping(target = "coverUrl", source = "coverUrl", qualifiedByName = "toCdnUrl")
    AlbumResponse toResponse(Album album);
}
