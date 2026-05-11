package com.example.soundscape_app.dto.response.global;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class GlobalErrorResponse {
    private String message;
    private LocalDateTime timestamp;
    private int statusCode;

    public GlobalErrorResponse(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
        this.timestamp = LocalDateTime.now();
    }

}
