package ru.yandex.practicum.filmorate.repository;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.mapper.ReviewResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class JdbcReviewRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcReviewRepository.class);

    private static final String INSERT_REVIEW_SQL =
            "INSERT INTO review (content, film_id, user_id, is_positive) " +
                    "VALUES (:content, :film_id, :user_id, :is_positive)";

    private static final String UPDATE_REVIEW_SQL =
            "UPDATE review SET content=:content, is_positive=:is_positive WHERE review_id = :review_id";

    private static final String CHECK_REVIEW_EXIST_WITH_FILMID_USERID =
            "SELECT COUNT(*) AS ROW_COUNT FROM review WHERE film_id = :film_id AND user_id = :user_id";

    private static final String SELECT_REVIEW_BY_FILM_ID =
            "SELECT rs.REVIEW_ID, rs.CONTENT,rs.FILM_ID, rs.USER_ID,rs.IS_POSITIVE, SUM(rl.SCORE) AS USEFUL " +
                    "FROM (SELECT * FROM REVIEW WHERE film_id=:film_id " +
                    "ORDER BY review_id LIMIT :count) AS rs " +
                    "LEFT JOIN REVIEW_LIKE rl " +
                    "ON rs.REVIEW_ID = rl.REVIEW_ID " +
                    "GROUP BY rs.REVIEW_ID;";

    private static final String SELECT_REVIEW_BY_ID_SQL =
            "SELECT rs.REVIEW_ID, rs.CONTENT,rs.FILM_ID, rs.USER_ID, rs.IS_POSITIVE, SUM(rl.SCORE) AS USEFUL " +
                    "FROM (SELECT * FROM REVIEW WHERE REVIEW_ID=:review_id) AS rs " +
                    "LEFT JOIN REVIEW_LIKE rl " +
                    "ON rs.REVIEW_ID = rl.REVIEW_ID " +
                    "GROUP BY rs.REVIEW_ID;";

    private static final String DELETE_REVIEW_BY_ID_SQL =
            "DELETE FROM review WHERE review_id=:review_id";

    private static final String ADD_LIKE_DISLIKE = "MERGE INTO review_like (review_id, user_id, score) " +
            "key(review_id, user_id) VALUES (:review_id, :user_id, :score)";

    private static final String DELETE_LIKE_DISLIKE = "DELETE FROM review_like where review_id=:review_id and user_id=:user_id" +
            " AND score=:score;";

    private final NamedParameterJdbcOperations jdbc;
    private final ReviewResultSetExtractor reviewResultSetExtractor;

    public Review save(Review review) {
        log.debug("Saving review: {}", review);
        System.out.println(review.getIsPositive().toString());
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("film_id", review.getFilmId())
                .addValue("user_id", review.getUserId())
                .addValue("is_positive", review.getIsPositive());


        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(INSERT_REVIEW_SQL, params, keyHolder, new String[]{"review_id"});

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to get generated key for review");
        }
        int id = key.intValue();
        review.setReviewId(id);

        log.debug("Review saved: {}", review);
        return review;
    }

    public Boolean checkReviewAlreadyExist(Review review) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("film_id", review.getFilmId())
                .addValue("user_id", review.getUserId());
        return jdbc.queryForObject(CHECK_REVIEW_EXIST_WITH_FILMID_USERID, params, Integer.class) > 0;
    }

    public Review update(Review review) {
        log.debug("Update review: {}", review);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("is_positive", review.getIsPositive())
                .addValue("review_id", review.getReviewId());

        jdbc.update(UPDATE_REVIEW_SQL, params);
        log.debug("Review updated: {}", review);
        return findById(review.getReviewId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Обзор с id = " + review.getReviewId() + " не найден"));
    }

    public Optional<Review> findById(int reviewId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", reviewId);
        List<Review> reviews = jdbc.query(SELECT_REVIEW_BY_ID_SQL, params, reviewResultSetExtractor);
        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.getFirst());
    }

    public List<Review> findReviewByFilmId(Integer filmId, Integer count) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("film_id", filmId)
                .addValue("count", count);
        return jdbc.query(SELECT_REVIEW_BY_FILM_ID, params, reviewResultSetExtractor);
    }

    public Integer deleteReviewById(Integer reviewId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", reviewId);
        return jdbc.update(DELETE_REVIEW_BY_ID_SQL, params);
    }

    public void addLike(Integer id, Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", id)
                .addValue("user_id", userId)
                .addValue("score", 1);
        jdbc.update(ADD_LIKE_DISLIKE, params);
    }

    public void addDisLike(Integer id, Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", id)
                .addValue("user_id", userId)
                .addValue("score", -1);
        jdbc.update(ADD_LIKE_DISLIKE, params);
    }

    public void deleteLike(Integer reviewId, Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", reviewId)
                .addValue("user_id", userId)
                .addValue("score", 1);
        jdbc.update(DELETE_LIKE_DISLIKE, params);
    }

    public void deleteDisLike(Integer id, Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", id)
                .addValue("user_id", userId)
                .addValue("score", -1);
        jdbc.update(DELETE_LIKE_DISLIKE, params);
    }

}
