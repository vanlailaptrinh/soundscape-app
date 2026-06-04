package com.example.soundscape_app.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistWithListSongResponse {
    PlaylistResponse playlist;
    List<SongAndArtistResponse> songAndArtistResponses;
}
