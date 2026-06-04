package com.example.soundscape_app.dto.response.song;

import com.example.soundscape_app.dto.response.user.ArtistResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongAndArtistResponse {
    private SongResponse song;
    private ArtistResponse artist;
}
