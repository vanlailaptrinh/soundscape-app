package com.example.soundscape_app.dto.request.song;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlaylistRequest {
    @NotNull(message = "songId is required")
    private Long songId;
}
