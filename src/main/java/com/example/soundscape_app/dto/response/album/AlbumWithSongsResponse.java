package com.example.soundscape_app.dto.response.album;

import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class AlbumWithSongsResponse {
    AlbumResponse album;
    List<SongResponse> songs;
    ArtistResponse artist;

}
