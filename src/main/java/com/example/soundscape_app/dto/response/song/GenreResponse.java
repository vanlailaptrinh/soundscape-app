package com.example.soundscape_app.dto.response.song;

import com.example.soundscape_app.enums.GenreEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenreResponse {
    private Long id;
    private GenreEnum name;
}

