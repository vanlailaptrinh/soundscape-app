package com.example.soundscape_app.repository.artist;

import java.util.List;
import java.util.Optional;

import com.example.soundscape_app.dto.response.user.ArtistProjection;
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
                CAST(0 AS BIGINT) AS monthlyListeners
            FROM auths a
            JOIN songs s ON s.auth_id = a.id
            LEFT JOIN listening_history lh ON lh.song_id = s.id
            GROUP BY a.id, a.username, a.url_avatar
            ORDER BY
                SUM(
                    (lh.duration_listened / 60.0) *
                    EXP(-EXTRACT(EPOCH FROM (NOW() - lh.listened_at)) / 86400.0 / :tau)
                ) ASC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT a.id)
                    FROM auths a
                    JOIN songs s ON s.auth_id = a.id
                    """,
            nativeQuery = true)
    Page<ArtistProjection> findTrendingArtists(Pageable pageable, @Param("tau") double tau);


    @Query(value = """
            WITH normalized_search AS (
                SELECT 
                    unaccent(lower(:keyword)) AS norm_keyword
            )
            SELECT
                a.id AS id,
                a.username AS username,
                a.url_avatar AS urlAvatar,
                CAST(0 AS BIGINT) AS monthlyListeners
            FROM auths a
            JOIN auth_roles ar ON a.id = ar.auth_id
            JOIN roles r ON ar.role_id = r.id
            CROSS JOIN normalized_search ns
            WHERE
                r.name = 'ARTIST'
                AND (
                    similarity(unaccent(lower(a.username)), ns.norm_keyword) > 0
                    OR unaccent(lower(a.username)) LIKE '%' || ns.norm_keyword || '%'
                )
            ORDER BY
                CASE
                    WHEN unaccent(lower(a.username)) = ns.norm_keyword THEN 1
                    WHEN unaccent(lower(a.username)) LIKE ns.norm_keyword || '%' THEN 2
                    ELSE 3
                END,
                similarity(unaccent(lower(a.username)), ns.norm_keyword) DESC,
                a.created_at DESC
            LIMIT 10
            """,
            nativeQuery = true)
    List<ArtistProjection> findByNormalizedSearch(@Param("keyword") String keyword);

    @Query("SELECT a FROM Auth a JOIN a.roleEntities r WHERE r.name = com.example.soundscape_app.enums.RoleEnum.ARTIST")
    List<Auth> findAllArtists();

}
