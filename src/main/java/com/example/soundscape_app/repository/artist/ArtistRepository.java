package com.example.soundscape_app.repository.artist;

import java.util.List;
import java.util.Optional;

import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.entity.auth.Auth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArtistRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findBySongsId(Long songId);

    @Query(value = """
            SELECT
                a.id AS id,
                a.username AS username,
                a.url_avatar AS urlAvatar,
                CAST(0 AS SIGNED) AS monthlyListeners
            FROM auths a
            JOIN songs s ON s.auth_id = a.id
            LEFT JOIN listening_history lh ON lh.song_id = s.id
            GROUP BY a.id, a.username, a.url_avatar
            ORDER BY
                SUM(
                    (lh.duration_listened / 60.0) *
                    EXP(-TIMESTAMPDIFF(SECOND, lh.listened_at, NOW()) / 86400.0 / :tau)
                ) ASC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT a.id)
                    FROM auths a
                    JOIN songs s ON s.auth_id = a.id
                    """,
            nativeQuery = true)
    Page<ArtistResponse> findTrendingArtists(Pageable pageable, @Param("tau") double tau);


    @Query(value = """
            WITH normalized_search AS (
                SELECT
                    LOWER(:keyword) AS norm_keyword
            )
            SELECT
                a.id AS id,
                a.username AS username,
                a.url_avatar AS urlAvatar,
                CAST(0 AS SIGNED) AS monthlyListeners
            FROM auths a
            JOIN auth_roles ar ON a.id = ar.auth_id
            JOIN roles r ON ar.role_id = r.id
            CROSS JOIN normalized_search ns
            WHERE
                r.name = 'ARTIST'
                AND (
                    LOWER(a.username) LIKE CONCAT('%', ns.norm_keyword, '%')
                )
            ORDER BY
                CASE
                    WHEN LOWER(a.username) = ns.norm_keyword THEN 1
                    WHEN LOWER(a.username) LIKE CONCAT(ns.norm_keyword, '%') THEN 2
                    ELSE 3
                END,
                a.created_at DESC
            LIMIT 10
            """,
            nativeQuery = true)
    List<ArtistResponse> findByNormalizedSearch(@Param("keyword") String keyword);


}
