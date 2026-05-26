package com.example.soundscape_app.repository.album;

import com.example.soundscape_app.dto.response.album.AlbumTrendingResponse;
import com.example.soundscape_app.entity.album.Album;
import com.example.soundscape_app.entity.song.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findById(Long id);

    List<Album> findByAuthId(Long authId);

    @Query(value = """
            SELECT al.id,
                   al.cover_url AS coverUrl,
                   al.name AS albumName,
                   a.id AS artistId,
                   a.userName AS username
            FROM albums al
            LEFT JOIN album_items ai ON ai.album_id = al.id
            LEFT JOIN songs s ON s.id = ai.song_id
            LEFT JOIN listening_history lh ON lh.song_id = s.id
            JOIN auths a ON al.auth_id = a.id
            GROUP BY al.id, al.cover_url, al.name, a.userName, a.id
            ORDER BY SUM(
                       (lh.duration_listened / 60.0) *
                       EXP(-EXTRACT(EPOCH FROM (NOW() - lh.listened_at)) / 86400.0 / 7)
                   ) ASC 
            """, nativeQuery = true)
    Page<AlbumTrendingResponse> getAlbumTrending(Pageable pageable);

    @Query("SELECT ai.song FROM Album a JOIN a.albumItems ai WHERE a.id = :albumId")
    List<Song> findAllSongsByAlbumId(@Param("albumId") Long albumId);

    @Query(value = """
            WITH normalized_search AS (
                SELECT 
                    unaccent(lower(:keyword)) AS norm_keyword,
                    NULL::tsquery AS norm_tsquery
            )
            SELECT 
                a.id AS id,
                a.cover_url AS coverUrl,
                a.name AS name,
                au.id AS artistId,
                au.username AS username
            FROM albums a
            JOIN auths au ON a.auth_id = au.id
            CROSS JOIN normalized_search ns
            WHERE 
                similarity(unaccent(lower(a.name)), ns.norm_keyword) > 0
                OR unaccent(lower(a.name)) LIKE '%' || ns.norm_keyword || '%'
            ORDER BY 
                CASE 
                    WHEN unaccent(lower(a.name)) = ns.norm_keyword THEN 1
                    WHEN unaccent(lower(a.name)) LIKE ns.norm_keyword || '%' THEN 2
                    ELSE 3
                END,
                similarity(unaccent(lower(a.name)), ns.norm_keyword) DESC,
                a.created_at DESC
            LIMIT 10
            """,
            nativeQuery = true)
    List<AlbumTrendingResponse> findByNormalizedSearch(@Param("keyword") String keyword);

}

