package com.example.soundscape_app.dto.response.song;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SongTrendingResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private String author;
    private Long artistId;
    private String username;

}