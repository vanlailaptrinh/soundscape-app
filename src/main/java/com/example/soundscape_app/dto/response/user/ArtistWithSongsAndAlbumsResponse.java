package com.spotify.dto.response.user;

import com.spotify.dto.response.album.AlbumResponse;
import com.spotify.dto.response.song.SongResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistWithSongsAndAlbumsResponse {
    private ArtistResponse artist;
    private List<SongResponse> songs;
    private List<AlbumResponse> album;
}
