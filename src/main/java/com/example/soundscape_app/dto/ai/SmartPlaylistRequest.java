package com.example.soundscape_app.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record SmartPlaylistRequest(@NotBlank String prompt) {
}
