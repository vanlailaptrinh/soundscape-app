package com.example.soundscape_app.dto.request.song;

import com.example.soundscape_app.enums.FollowEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequest {
    @NotNull(message = "id is required")
    private Long id;

    @NotNull(message = "type is required")
    private FollowEnum type;
}
