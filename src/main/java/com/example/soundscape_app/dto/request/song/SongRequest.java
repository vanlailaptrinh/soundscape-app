package com.example.soundscape_app.dto.request.song;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongRequest {
    @NotNull(message = "File is required")
    private MultipartFile fileMedia;

    private MultipartFile fileImage;  // file ảnh bìa
    private String title;
    private String author;

    private Long albumId;

    private List<Long> genreIds;

    public List<Long> getGenreIds() {
        return (genreIds == null || genreIds.isEmpty())
                ? List.of(1L)
                : genreIds;
    }
}
