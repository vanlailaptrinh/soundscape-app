package com.example.soundscape_app.util;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class S3UtilTest {

    private final S3Util s3Util = new S3Util(null);

    @Test
    void toCdnUrlConvertsRegionalS3Url() {
        configureCdn();

        String url = s3Util.toCdnUrl(
                "https://soundscape-app-storage.s3.ap-southeast-1.amazonaws.com/asset/video/comemyway.mp4"
        );

        assertThat(url).isEqualTo("https://cdn.vantrandigital.id.vn/asset/video/comemyway.mp4");
    }

    @Test
    void toCdnUrlConvertsGlobalS3Url() {
        configureCdn();

        String url = s3Util.toCdnUrl(
                "https://soundscape-app-storage.s3.amazonaws.com/asset/music/song.mp3"
        );

        assertThat(url).isEqualTo("https://cdn.vantrandigital.id.vn/asset/music/song.mp3");
    }

    @Test
    void toCdnUrlKeepsExternalUrl() {
        configureCdn();

        String url = s3Util.toCdnUrl("https://picsum.photos/seed/song7/300/300");

        assertThat(url).isEqualTo("https://picsum.photos/seed/song7/300/300");
    }

    private void configureCdn() {
        ReflectionTestUtils.setField(s3Util, "bucketName", "soundscape-app-storage");
        ReflectionTestUtils.setField(s3Util, "cdnBaseUrl", "https://cdn.vantrandigital.id.vn");
    }
}
