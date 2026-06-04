package com.example.soundscape_app.dto.response.song;

import com.example.soundscape_app.enums.GenreEnum;
import com.example.soundscape_app.enums.MediaEnum;
import com.example.soundscape_app.enums.SongStatusEnum;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongDetailResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private String author;
    private int playCount;
    private LocalDateTime createdAt;
    private List<GenreEnum> genres;
    private MediaEnum type;
    private Double ratingAvg;
    private Integer ratingCount;
    private SongStatusEnum status;

    // Thông tin Auth (chỉ lấy tên)
    private Long authId;
    private String authUsername;
    private List<ArtistResponse> collaborators;
}