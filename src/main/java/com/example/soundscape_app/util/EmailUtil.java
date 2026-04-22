package com.spotify.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@AllArgsConstructor
public class EmailUtil {

    private final JavaMailSender mailSender;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }


    public void sendAccountInfoEmail(String email, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Your Auth Information");

            String content = String.format(
                    "Dear %s,\n\n" +
                            "Your auth has been created.\n\n" +
                            "Username: %s\n" +
                            "Password: %s\n\n" +
                            "Please change your password after first login.\n\n" +
                            "Best regards,\n" +
                            "Support Team",
                    email, email, password
            );

            helper.setText(content.replace("\n", "<br>"), true); // HTML email
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send auth information email", e);
        }
    }

    @Async
    public void sendEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Your Verification Code");
            helper.setText("Your verification code is: " + code);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public static void valid(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required!");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format!");
        }
    }
}
