package com.example.soundscape_app.service.auth;

public interface EmailService {
    void sendHtml(String to, String subject, String htmlBody);
}
