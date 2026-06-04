package com.example.soundscape_app.dto.response.song;

import com.example.soundscape_app.enums.SongStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListSongResponse {
    private Long id;
    private String title;
    private String artistEmail;
    private SongStatusEnum status;
}
