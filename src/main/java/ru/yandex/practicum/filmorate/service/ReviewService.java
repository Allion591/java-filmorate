package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FeedService;
import ru.yandex.practicum.filmorate.interfaces.FilmRepository;
import ru.yandex.practicum.filmorate.interfaces.UserRepository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.JdbcReviewRepository;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class ReviewService {
    final JdbcReviewRepository reviewRepository;
    final FilmRepository filmRepository;
    final UserRepository userRepository;
    final FeedService feedService;

    public Review createReview(Review review) {
        if (review.getFilmId() == null) {
            throw new NoSuchElementException("filmId = null");
        }
        if (review.getUserId() == null) {
            throw new NoSuchElementException("userId = null");
        }
        filmRepository.getFilmById(review.getFilmId());
        userRepository.getUserById(review.getUserId());
        if (reviewRepository.checkReviewAlreadyExist(review) == true) {
            throw new ValidationException("Обзор фильма с id=" + review.getFilmId()
                    + " созданный пользователем с id=" + review.getUserId() + " уже существет");
        }
        Review review1 = reviewRepository.save(review);
        feedService.saveReview(review1);
        return review1;
    }

    public Review updateReview(Review review) {
        final Review existReview = reviewRepository.findById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + review.getReviewId() + " не найден"));
        existReview.setContent(review.getContent());
        existReview.setIsPositive(review.getIsPositive());
        Review review1 = reviewRepository.update(existReview);
        feedService.updateReview(review1);
        return review1;
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Обзор с id = " + id + " не найден"));
    }

    public ResponseEntity<Review> deleteReviewById(Long reviewId) {
        final Review existReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.deleteReviewById(reviewId);
        feedService.deleteReview(existReview);
        return new ResponseEntity<>(existReview, HttpStatus.OK);
    }

    public List<Review> getReviewByFilmId(Long filmId, Long count) {
        if (filmId != null) {
            if (filmId <= 0) {
                throw new ValidationException("FilmId должен быть положительным");
            }
            filmRepository.getFilmById(filmId);
        }
        return reviewRepository.findReviewByFilmId(filmId, count);
    }

    public Review addLike(Long reviewId, Long userId) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.addLike(reviewId, userId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
    }

    public Review deleteLike(Long reviewId, Long userId) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.deleteLike(reviewId, userId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
    }

    public Review addDislike(Long reviewId, Long userId) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.addDisLike(reviewId, userId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
    }

    public Review deleteDislike(Long reviewId, Long userId) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));
        reviewRepository.deleteDisLike(reviewId, userId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Обзор с reviewId = " + reviewId + " не найден"));

    }
}