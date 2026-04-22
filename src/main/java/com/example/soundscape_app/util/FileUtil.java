package com.spotify.util;

import com.spotify.enums.MediaEnum;
import com.spotify.exception.InvalidFileTypeException;
import org.mp4parser.IsoFile;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.List;

public class FileUtil {

    private static final List<String> IMAGE_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex != -1) ? fileName.substring(dotIndex + 1).toLowerCase() : "";
    }

    public static boolean isAudioFile(MultipartFile file) {
        String contentType = file.getContentType();

        // Kiểm tra MIME type
        if (contentType != null && contentType.startsWith("audio/")) {
            return true;
        }

        // Kiểm tra phần mở rộng
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = getFileExtension(fileName);
            switch (extension) {
                case "mp3":
                case "wav":
                case "aac":
                case "flac":
                case "ogg":
                case "m4a":
                    return true;
            }
        }

        return false;
    }

    public static boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();

        // Kiểm tra MIME type
        if (contentType != null && contentType.startsWith("video/")) {
            return true;
        }

        // Kiểm tra phần mở rộng
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = getFileExtension(fileName);
            switch (extension) {
                case "mp4":
                case "mkv":
                case "mov":
                case "avi":
                case "flv":
                case "wmv":
                case "webm":
                    return true;
            }
        }

        return false;
    }

    public static boolean isAudioOrVideoFile(MultipartFile file) {
        return !isAudioFile(file) && !isVideoFile(file);
    }

    public static MediaEnum getMediaType(MultipartFile file) {
        if (isAudioFile(file)) {
            return MediaEnum.AUDIO;
        } else if (isVideoFile(file)) {
            return MediaEnum.VIDEO;
        } else {
            return MediaEnum.UNKNOWN;
        }
    }


    public static void validateAudioOrVideoFileOrThrow(MultipartFile file) {
        if (file == null || FileUtil.isAudioOrVideoFile(file)) {
            throw new InvalidFileTypeException("File không hợp lệ.");
        }
    }

    public static void isImageFileOrThrow(MultipartFile file) {
        if (!FileUtil.isImageFile(file)) {
            throw new InvalidFileTypeException("File ảnh không hợp lệ.");
        }
    }


    public static boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        String contentType = file.getContentType();
        return contentType != null && IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    public static long getDurationInSeconds(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename().toLowerCase();

        if (filename.endsWith(".mp3") || filename.endsWith(".wav")) {
            // audio
            File tempFile = File.createTempFile("audio", filename);
            file.transferTo(tempFile);

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(tempFile);
            long frames = audioInputStream.getFrameLength();
            float frameRate = audioInputStream.getFormat().getFrameRate();
            tempFile.delete();

            return (long) (frames / frameRate);

        } else if (filename.endsWith(".mp4")) {
            // video
            File tempFile = File.createTempFile("video", ".mp4");
            file.transferTo(tempFile);

            IsoFile isoFile = new IsoFile(tempFile.getAbsolutePath());
            double lengthInSeconds = (double) isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                    isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
            tempFile.delete();

            return (long) lengthInSeconds;

        } else {
            throw new IllegalArgumentException("Unsupported file type: " + filename);
        }
    }

}
