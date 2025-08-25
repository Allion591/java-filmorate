package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReviewResultSetExtractor implements ResultSetExtractor<List<Review>> {

    @Override
    public List<Review> extractData(ResultSet rs) throws SQLException {
        Map<Integer, Review> reviewMap = new LinkedHashMap<>();
        while (rs.next()) {
            int id = rs.getInt("review_id");
            if (!reviewMap.containsKey(id)) {
                Review review = new Review();
                review.setReviewId(id);
                review.setContent(rs.getString("content"));
                review.setFilmId(rs.getLong("film_id"));
                review.setUserId(rs.getLong("user_id"));
                review.setUseful(rs.getInt("useful"));
                //review.setIsPositive(rs.getObject("useful") != null && rs.getInt("useful") > 0);
                review.setIsPositive(rs.getBoolean("is_positive"));
                reviewMap.put(id, review);
            }
        }
        return new ArrayList<>(reviewMap.values());
    }
}