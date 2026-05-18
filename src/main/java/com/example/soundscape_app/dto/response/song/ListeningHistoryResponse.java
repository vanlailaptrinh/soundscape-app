package com.example.soundscape_app.dto.response.song;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListeningHistoryResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private Long artistId;
}
