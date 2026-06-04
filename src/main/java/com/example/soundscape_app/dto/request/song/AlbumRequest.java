package com.example.soundscape_app.dto.request.song;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumRequest {
    private String name;
    private MultipartFile fileCover;
    private String description;
}
