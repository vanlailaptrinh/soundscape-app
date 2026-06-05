package com.example.soundscape_app.util;

import com.example.soundscape_app.enums.MediaEnum;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.mapstruct.Named;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class S3Util {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.asset.music}")
    private String musicFolder;

    @Value("${aws.s3.asset.image}")
    private String imageFolder;

    @Value("${aws.s3.asset.video}")
    private String videoFolder;

    @Value("${app.cdn.base-url:}")
    private String cdnBaseUrl;

    public S3Util(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFileMusic(MultipartFile file) {
        return uploadFile(file, musicFolder, "audio/mpeg", null);
    }

    public String uploadFileImage(MultipartFile file) {
        return uploadFile(file, imageFolder, "image/jpeg", "jpg");
    }

    public String uploadFileVideo(MultipartFile file) {
        return uploadFile(file, videoFolder, "video/mp4", null);
    }

    private String uploadFile(MultipartFile file, String folder, String contentType, String forceImageFormat) {
        String extension = forceImageFormat != null
                ? forceImageFormat
                : FileUtil.getFileExtension(file.getOriginalFilename());

        String fileName = System.currentTimeMillis() + "." + extension;
        String key = folder + fileName;

        try {
            byte[] fileBytes;

            if (forceImageFormat != null) {
                BufferedImage image = ImageIO.read(file.getInputStream());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                Thumbnails.of(image)
                        .size(image.getWidth(), image.getHeight())
                        .outputFormat(forceImageFormat)
                        .outputQuality(0.9)
                        .toOutputStream(outputStream);

                fileBytes = outputStream.toByteArray();
            } else {
                fileBytes = file.getBytes();
            }

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .contentDisposition("inline")
                            .cacheControl("public, max-age=31536000")
                            .build(),
                    RequestBody.fromBytes(fileBytes)
            );

            return "https://" + bucketName + ".s3.amazonaws.com/" + key;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage(), e);
        }
    }

    @Named("uploadImageFromUrl")
    public String uploadImageFromUrl(String imageUrl) {
        String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
        String key = imageFolder + fileName;

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed to download image: HTTP " + connection.getResponseCode());
            }

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] fileBytes = inputStream.readAllBytes();

                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .contentType("image/jpeg")
                                .contentDisposition("inline")
                                .cacheControl("public, max-age=31536000")
                                .build(),
                        RequestBody.fromBytes(fileBytes)
                );

                return "https://" + bucketName + ".s3.amazonaws.com/" + key;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error uploading image from URL: " + e.getMessage(), e);
        }
    }

    @Named("toCdnUrl")
    public String toCdnUrl(String url) {
        if (url == null || url.isBlank() || cdnBaseUrl == null || cdnBaseUrl.isBlank()) {
            return url;
        }

        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (!isOwnS3Host(host)) {
                return url;
            }

            String cdnBase = cdnBaseUrl.endsWith("/")
                    ? cdnBaseUrl.substring(0, cdnBaseUrl.length() - 1)
                    : cdnBaseUrl;
            String query = uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "";
            return cdnBase + uri.getRawPath() + query;
        } catch (IllegalArgumentException e) {
            return url;
        }
    }

    private boolean isOwnS3Host(String host) {
        if (host == null || bucketName == null || bucketName.isBlank()) {
            return false;
        }

        String globalS3Host = bucketName + ".s3.amazonaws.com";
        return host.equals(globalS3Host)
                || (host.startsWith(bucketName + ".s3.") && host.endsWith(".amazonaws.com"));
    }

    public String uploadFile(MultipartFile file) {
        MediaEnum type = FileUtil.getMediaType(file);
        if (type == MediaEnum.AUDIO) return uploadFileMusic(file);
        if (type == MediaEnum.VIDEO) return uploadFileVideo(file);
        return null;
    }

    public String uploadImageIfPresent(MultipartFile fileImage) {
        return fileImage != null ? uploadFileImage(fileImage) : null;
    }
}
