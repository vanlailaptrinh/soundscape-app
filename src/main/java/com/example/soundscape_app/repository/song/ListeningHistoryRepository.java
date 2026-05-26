package com.example.soundscape_app.repository.song;

import com.example.soundscape_app.dto.hepler.MonthlyPlayCount;
import com.example.soundscape_app.dto.response.song.AppListeningChartPoint;
import com.example.soundscape_app.dto.response.song.DailyListeningStat;
import com.example.soundscape_app.dto.response.song.DailyListeningTime;
import com.example.soundscape_app.dto.response.song.TopSongStat;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.song.ListeningHistory;
import com.example.soundscape_app.entity.song.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
    Optional<ListeningHistory> findTopByAuthAndSongOrderByListenedAtDesc(Auth auth, Song song);

    List<ListeningHistory> findTop100ByAuth_IdOrderByListenedAtDesc(Long userId);

    @Query(value = """
            SELECT 
                EXTRACT(YEAR FROM lh.listened_at) AS year,
                EXTRACT(MONTH FROM lh.listened_at) AS month,
                COUNT(lh.id) AS total
            FROM listening_history lh
            JOIN songs s ON lh.song_id = s.id
            WHERE s.auth_id = :artistId
            AND lh.listened_at < DATE_TRUNC('month', CURRENT_DATE)
            GROUP BY 
                EXTRACT(YEAR FROM lh.listened_at), 
                EXTRACT(MONTH FROM lh.listened_at)
            ORDER BY year DESC, month DESC
            """, nativeQuery = true)
    List<MonthlyPlayCount> findMonthlyPlayCountsByArtist(@Param("artistId") Long artistId);

    @Query(value = """
                SELECT 
                    TO_CHAR(lh.listened_at, 'YYYY-MM-DD') AS day,
                    COUNT(*) AS plays
                FROM listening_history lh
                WHERE lh.song_id = :songId
                  AND lh.listened_at >= (CURRENT_DATE - (:days * INTERVAL '1 day'))
                GROUP BY TO_CHAR(lh.listened_at, 'YYYY-MM-DD')
                ORDER BY day
            """, nativeQuery = true)
    List<DailyListeningStat> getSongDailyStats(
            @Param("songId") Long songId,
            @Param("days") int days
    );

    @Query(value = """
                SELECT 
                    s.id AS songId,
                    s.title AS title,
                    a.username AS artist,
                    COUNT(lh.id) AS listeningCount
                FROM listening_history lh
                JOIN songs s ON lh.song_id = s.id
                JOIN auths a ON s.auth_id = a.id
                WHERE lh.listened_at >= (CURRENT_DATE - (:days || ' days')::INTERVAL)
                GROUP BY s.id, s.title, a.username
                ORDER BY listeningCount DESC
                LIMIT 10
            """, nativeQuery = true)
    List<TopSongStat> getTopSongsApp(@Param("days") int days);

    @Query(value = """
                SELECT 
                    TO_CHAR(lh.listened_at, 'YYYY-MM-DD') AS date,
                    COUNT(*) AS count
                FROM listening_history lh
                WHERE lh.listened_at >= (CURRENT_DATE - (:days || ' days')::INTERVAL)
                GROUP BY date
                ORDER BY date
            """, nativeQuery = true)
    List<AppListeningChartPoint> getAppDailyListeningStats(@Param("days") int days);

    @Query(value = """
                SELECT
                    TO_CHAR(lh.listened_at, 'YYYY-MM-DD') AS day,
                    CAST(SUM(lh.duration_listened) AS BIGINT) AS totalDuration
                FROM listening_history lh
                WHERE lh.auth_id = :userId
                  AND lh.listened_at >= (CURRENT_DATE - (:days * INTERVAL '1 day'))
                GROUP BY TO_CHAR(lh.listened_at, 'YYYY-MM-DD')
                ORDER BY day
            """, nativeQuery = true)
    List<DailyListeningTime> getUserDailyListeningTime(
            @Param("userId") Long userId,
            @Param("days") int days
    );

}
