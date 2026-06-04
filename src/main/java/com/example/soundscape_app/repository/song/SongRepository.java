package com.example.soundscape_app.repository.song;

import com.example.soundscape_app.dto.response.song.ListeningHistoryResponse;
import com.example.soundscape_app.dto.response.song.SongTrendingResponse;
import com.example.soundscape_app.entity.song.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    @Query(value = """
            SELECT s.id,
                   s.title,
                   s.image_url AS imageUrl,
                   s.author,
                   auths.id AS artistId,
                   auths.username AS username
            FROM songs s
            JOIN auths ON s.auth_id = auths.id
            LEFT JOIN album_items ai ON ai.song_id = s.id
            LEFT JOIN albums a ON ai.album_id = a.id
            LEFT JOIN (
                SELECT lh.song_id,
                       SUM(
                           (lh.duration_listened / 60.0) *
                           EXP(-EXTRACT(EPOCH FROM (NOW() - lh.listened_at)) / 86400.0 / :tau)
                       ) AS score
                FROM listening_history lh
                GROUP BY lh.song_id
            ) t ON t.song_id = s.id
            WHERE s.status <> 'BANNED'
            ORDER BY COALESCE(t.score, 0) DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM songs s
            WHERE s.status <> 'BANNED'
            """, nativeQuery = true)
    Page<SongTrendingResponse> findTrendingSongs(@Param("tau") double tau, Pageable pageable);

    List<Song> findByAuthId(Long authId);

    @Query(value = """
            SELECT sub.id,
                   sub.title,
                   sub.imageUrl,
                   sub.artistId
            FROM (
                SELECT
                    s.id,
                    s.title,
                    s.image_url AS imageUrl,
                    lh.listened_at,
                    a.id AS artistId,
                    ROW_NUMBER() OVER (PARTITION BY lh.song_id ORDER BY lh.listened_at DESC) AS rn
                FROM listening_history lh
                JOIN songs s ON lh.song_id = s.id
                JOIN auths a ON s.auth_id = a.id
                WHERE lh.auth_id = :authId
                  AND s.status <> 'BANNED'
            ) sub
            WHERE sub.rn = 1
            ORDER BY sub.listened_at DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM (
                SELECT ROW_NUMBER() OVER (PARTITION BY lh.song_id ORDER BY lh.listened_at DESC) AS rn
                FROM listening_history lh
                JOIN songs s ON lh.song_id = s.id
                WHERE lh.auth_id = :authId
                  AND s.status <> 'BANNED'
            ) sub
            WHERE sub.rn = 1
            """, nativeQuery = true)
    Page<ListeningHistoryResponse> findUniqueListeningHistory(@Param("authId") Long authId, Pageable pageable);

    @Query(value = """
            WITH normalized_search AS (
                SELECT
                    unaccent(lower(:keyword)) AS norm_keyword,
                    NULL::tsquery AS norm_tsquery
            )
            SELECT
                s.id AS id,
                s.title AS title,
                s.image_url AS imageUrl,
                a.username AS author,
                a.id AS artistId,
                a.username AS username
            FROM songs s
            JOIN auths a ON s.auth_id = a.id
            CROSS JOIN normalized_search ns
            WHERE
                s.status <> 'BANNED'
                AND (
                    similarity(unaccent(lower(s.title)), ns.norm_keyword) > 0
                    OR unaccent(lower(s.title)) LIKE '%' || ns.norm_keyword || '%'
                )
            ORDER BY
                CASE
                    WHEN unaccent(lower(s.title)) = ns.norm_keyword THEN 1
                    WHEN unaccent(lower(s.title)) LIKE ns.norm_keyword || '%' THEN 2
                    ELSE 3
                END,
                similarity(unaccent(lower(s.title)), ns.norm_keyword) DESC,
                s.created_at DESC
            LIMIT 10
            """, nativeQuery = true)
    List<SongTrendingResponse> findByNormalizedSearch(@Param("keyword") String keyword);

    @Query("SELECT COUNT(DISTINCT s) FROM Song s LEFT JOIN s.collaborators c WHERE (s.auth.id = :userId OR c.id = :userId) AND s.status <> 'BANNED'")
    long countSongsByUser(Long userId);

    @Query("SELECT DISTINCT s FROM Song s LEFT JOIN s.collaborators c WHERE s.auth.id = :authId OR c.id = :authId")
    List<Song> findByAuthIdOrCollaboratorId(@Param("authId") Long authId);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   s.image_url AS imageUrl,
                   s.author,
                   auths.id   AS artistId,
                   auths.username AS username
            FROM songs s
            JOIN auths ON s.auth_id = auths.id
            WHERE s.status <> 'BANNED'
            ORDER BY s.created_at DESC
            """, countQuery = """
            SELECT COUNT(*) FROM songs s WHERE s.status <> 'BANNED'
            """, nativeQuery = true)
    Page<SongTrendingResponse> findRecentSongs(Pageable pageable);

}
