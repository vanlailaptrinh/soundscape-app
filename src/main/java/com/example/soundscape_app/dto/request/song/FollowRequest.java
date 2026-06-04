package com.example.soundscape_app.dto.request.song;

import com.example.soundscape_app.enums.FollowEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequest {
    private Long id;
    private FollowEnum type;
}
