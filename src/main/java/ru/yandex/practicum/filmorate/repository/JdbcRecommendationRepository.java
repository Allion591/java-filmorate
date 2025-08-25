package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.interfaces.RecommendationRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Qualifier("jdbcRecommendationRepository")
@RequiredArgsConstructor
public class JdbcRecommendationRepository implements RecommendationRepository {

    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public List<Long> findRecommendedFilmIdsForUser(Long userId) {
        List<Long> uLikesList = jdbcOperations.query(
                "SELECT film_id FROM likes WHERE user_id = :userId",
                new MapSqlParameterSource("userId", userId),
                (rs, rowNum) -> rs.getLong("film_id")
        );
        Set<Long> uLikes = new HashSet<>(uLikesList);
        if (uLikes.isEmpty()) return List.of();

        Map<Long, Set<Long>> likesByUser = new HashMap<>();
        jdbcOperations.query(
                "SELECT user_id, film_id FROM likes WHERE user_id <> :userId",
                new MapSqlParameterSource("userId", userId),
                rs -> {
                    long otherId = rs.getLong("user_id");
                    long filmId = rs.getLong("film_id");
                    likesByUser.computeIfAbsent(otherId, k -> new HashSet<>()).add(filmId);
                }
        );

        record SimilarityScore(long userId, int common, int extra) {}

        Optional<Long> bestUserIdOpt = likesByUser.entrySet().stream()
                .map(e -> {
                    long otherId = e.getKey();
                    Set<Long> otherLikes = e.getValue();
                    int common = (int) otherLikes.stream().filter(uLikes::contains).count();
                    int extra = (int) otherLikes.stream().filter(f -> !uLikes.contains(f)).count();
                    return new SimilarityScore(otherId, common, extra);
                })
                .filter(sc -> sc.common() > 0 && sc.extra() > 0)
                .max(Comparator
                        .comparingInt(SimilarityScore::common)
                        .thenComparingInt(SimilarityScore::extra)
                        .thenComparingLong(SimilarityScore::userId)
                )
                .map(SimilarityScore::userId);

        if (bestUserIdOpt.isEmpty()) return List.of();
        long bestUserId = bestUserIdOpt.get();

        List<Long> candidates = likesByUser.getOrDefault(bestUserId, Set.of()).stream()
                .filter(f -> !uLikes.contains(f))
                .toList();
        if (candidates.isEmpty()) return List.of();

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("filmIds", candidates);
        List<Map<String, Object>> rows = jdbcOperations.queryForList(
                "SELECT film_id, COUNT(user_id) AS cnt FROM likes WHERE film_id IN (:filmIds) GROUP BY film_id",
                params
        );
        Map<Long, Integer> popularity = rows.stream().collect(Collectors.toMap(
                r -> ((Number) r.get("film_id")).longValue(),
                r -> ((Number) r.get("cnt")).intValue()
        ));

        return candidates.stream()
                .sorted(Comparator
                        .comparing((Long id) -> popularity.getOrDefault(id, 0)).reversed()
                        .thenComparing(Long::longValue))
                .toList();
    }
}
