package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.interfaces.RecommendationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class JdbcRecommendationRepositoryTest {

    @Autowired
    private NamedParameterJdbcOperations jdbc;

    @Autowired
    private RecommendationRepository recommendationRepository;

    private long insertUser(String email, String login, String name, LocalDate birthday) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (:email, :login, :name, :birthday)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("login", login)
                .addValue("name", name)
                .addValue("birthday", birthday);
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, params, kh, new String[]{"user_id"});
        return Objects.requireNonNull(kh.getKey()).longValue();
    }

    private long insertFilm(String name, String description, LocalDate releaseDate, long durationMinutes, long mpaId) {
        String sql = "INSERT INTO films (films_name, description, release_date, duration, mpa_id) " +
                "VALUES (:name, :description, :rd, :dur, :mpa)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("description", description)
                .addValue("rd", releaseDate)
                .addValue("dur", durationMinutes)
                .addValue("mpa", mpaId);
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, params, kh, new String[]{"film_id"});
        return Objects.requireNonNull(kh.getKey()).longValue();
    }

    private void like(long userId, long filmId) {
        String sql = "INSERT INTO likes (user_id, film_id) VALUES (:u, :f)";
        jdbc.update(sql, new MapSqlParameterSource().addValue("u", userId).addValue("f", filmId));
    }

    @Test
    void findRecommendedFilmIdsForUser_noLikes_returnsEmpty() {
        long u = insertUser("u@mail.com", "u", "U", LocalDate.of(1990,1,1));
        List<Long> rec = recommendationRepository.findRecommendedFilmIdsForUser(u);
        assertNotNull(rec);
        assertTrue(rec.isEmpty());
    }

    @Test
    void findRecommendedFilmIdsForUser_noSimilarUser_returnsEmpty() {
        long u = insertUser("u@mail.com", "u", "U", LocalDate.of(1990,1,1));
        long v = insertUser("v@mail.com", "v", "V", LocalDate.of(1991,2,2));
        long f1 = insertFilm("F1", "d", LocalDate.of(2010,1,1), 100, 1);
        long f2 = insertFilm("F2", "d", LocalDate.of(2011,1,1), 100, 1);
        like(u, f1);
        like(v, f2);
        List<Long> rec = recommendationRepository.findRecommendedFilmIdsForUser(u);
        assertTrue(rec.isEmpty());
    }

    @Test
    void findRecommendedFilmIdsForUser_basicAndPopularityOrder() {
        long a = insertUser("a@mail.com", "a", "A", LocalDate.of(1990,1,1));
        long b = insertUser("b@mail.com", "b", "B", LocalDate.of(1991,2,2));
        long c = insertUser("c@mail.com", "c", "C", LocalDate.of(1992,3,3));
        long d = insertUser("d@mail.com", "d", "D", LocalDate.of(1993,4,4));

        long f1 = insertFilm("F1", "d", LocalDate.of(2010,1,1), 100, 1);
        long f2 = insertFilm("F2", "d", LocalDate.of(2011,1,1), 100, 1);
        long f3 = insertFilm("F3", "d", LocalDate.of(2012,1,1), 100, 1);
        long f4 = insertFilm("F4", "d", LocalDate.of(2013,1,1), 100, 1);

        like(a, f1);

        like(b, f1);
        like(b, f2);
        like(b, f3);

        like(c, f1);
        like(c, f4);

        like(d, f2);

        List<Long> rec = recommendationRepository.findRecommendedFilmIdsForUser(a);
        assertEquals(List.of(f2, f3), rec, "Candidates must be sorted by popularity desc then id asc");
    }
}
