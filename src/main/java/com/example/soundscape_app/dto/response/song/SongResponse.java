package com.example.soundscape_app.dto.response.song;

import com.example.soundscape_app.enums.GenreEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongResponse {
    private String id;
    private String title;
    private String mediaUrl;
    private String imageUrl;
    private String author;
    private List<GenreEnum> genres;
    private Long duration;
    private String description;
}
