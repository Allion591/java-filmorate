package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FilmRepository;
import ru.yandex.practicum.filmorate.interfaces.UserRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.JdbcReviewRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class ReviewService {
    final JdbcReviewRepository reviewRepository;
    final FilmRepository filmRepository;
    final UserRepository userRepository;

    public Review createReview(Review review) {
        if (review.getFilmId() == null) {
            throw new NoSuchElementException("filmId = null");
        }
        if (review.getUserId() == null) {
            throw new NoSuchElementException("userId = null");
        }
        final Film existFilm = filmRepository.getFilmById(review.getFilmId());
        final User existUser = userRepository.getUserById(review.getUserId());
        if (reviewRepository.checkReviewAlreadyExist(review) == true) {
            throw new ValidationException("Обзор фильма с id=" + review.getFilmId()
                    + " созданный пользователем с id=" + review.getUserId() + " уже существет");
        }
        return reviewRepository.save(review);
    }

    public Review updateReview(Review review) {
        final Review existReview = reviewRepository.findById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + review.getReviewId() + " не найден"));
        existReview.setContent(review.getContent());
        existReview.setIsPositive(review.getIsPositive());
        return reviewRepository.update(existReview);
    }

    public Review getReviewById(Integer id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Обзор с id = " + id + " не найден"));
    }

    public ResponseEntity<Review> deleteReviewById(Integer reviewId) {
        final Review existReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.deleteReviewById(reviewId);
        return new ResponseEntity<>(existReview, HttpStatus.OK);
    }

    public List<Review> getReviewByFilmId(Integer filmId, Integer count) {
//        final Film existFilm = filmRepository.findById(filmId)
//                .orElseThrow(() -> new NoSuchElementException("Фильм с id = " + filmId + " не найден"));
        return reviewRepository.findReviewByFilmId(filmId, count);
    }

    public Review addLike(Integer reviewId, Integer userId) {
        final Review existReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.addLike(reviewId, userId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
    }

    public Review deleteLike(Integer reviewId, Integer userId) {
        final Review existReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.deleteLike(reviewId, userId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
    }

    public Review addDislike(Integer reviewId, Integer userId) {
        final Review existReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.addDisLike(reviewId, userId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
    }

    public Review deleteDislike(Integer reviewId, Integer userId) {
        final Review existReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.deleteDisLike(reviewId, userId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));

    }

}
