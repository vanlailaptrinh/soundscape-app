package com.example.soundscape_app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SoundscapeAppApplication {

	private static final Logger log = LoggerFactory.getLogger(SoundscapeAppApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SoundscapeAppApplication.class, args);
	}

	@Bean
	ApplicationRunner logBuildInfo() {
		return args -> log.info(
				"Soundscape backend started | imageTag={} | gitCommit={} | buildTime={}",
				getEnv("APP_IMAGE_TAG"),
				getEnv("APP_GIT_COMMIT"),
				getEnv("APP_BUILD_TIME")
		);
	}

	private static String getEnv(String key) {
		String value = System.getenv(key);
		return value == null || value.isBlank() ? "unknown" : value;
	}
}
