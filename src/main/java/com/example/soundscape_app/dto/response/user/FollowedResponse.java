package com.spotify.dto.response.user;

import com.spotify.dto.response.album.AlbumResponse;
import com.spotify.dto.response.song.PlaylistResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowedResponse {
    private List<PlaylistResponse> playlistFollowed;
    private List<ArtistResponse> artistFollowed;
    private List<AlbumResponse> albumFollowed;
}
