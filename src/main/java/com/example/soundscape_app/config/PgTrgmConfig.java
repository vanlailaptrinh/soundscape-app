package com.spotify.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PgTrgmConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void enablePgExtensions() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm;");
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS unaccent;");
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;;");

    }
}
