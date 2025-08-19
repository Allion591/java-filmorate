package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Like;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(JdbcLikeRepository.class)
@DisplayName("JdbcLikeRepositoryTest")
class JdbcLikeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcLikeRepository likeRepository;

    private final long filmId1 = 1L;
    private final long filmId2 = 2L;
    private final long userId1 = 1L;
    private final long userId2 = 2L;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("INSERT INTO films (film_id, films_name, description, release_date, duration, mpa_id) " +
                "VALUES (?, 'Film 1', 'Desc', '2000-01-01', 120, 1)", filmId1);
        jdbcTemplate.update("INSERT INTO films (film_id, films_name, description, release_date, duration, mpa_id) " +
                "VALUES (?, 'Film 2', 'Desc', '2000-01-01', 120, 1)", filmId2);

        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) " +
                "VALUES (?, 'user1@mail.com', 'login1', 'User 1', '1990-01-01')", userId1);
        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) " +
                "VALUES (?, 'user2@mail.com', 'login2', 'User 2', '1990-01-01')", userId2);
    }

    @Test
    @DisplayName("Должен добавлять лайк")
    void shouldAddLike() {
        likeRepository.addLike(filmId1, userId1);

        List<Like> likes = likeRepository.findLikesByFilmId(filmId1);
        assertThat(likes)
                .usingRecursiveComparison()
                .isEqualTo(List.of(new Like(filmId1, userId1)));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при добавлении лайка несуществующему фильму")
    void shouldThrowWhenAddingLikeToNonExistingFilm() {
        assertThatThrownBy(() -> likeRepository.addLike(999L, userId1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь или фильм не найдены");
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при добавлении лайка несуществующим пользователем")
    void shouldThrowWhenAddingLikeWithNonExistingUser() {
        assertThatThrownBy(() -> likeRepository.addLike(filmId1, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь или фильм не найдены");
    }

    @Test
    @DisplayName("Должен удалять лайк")
    void shouldRemoveLike() {
        likeRepository.addLike(filmId1, userId1);
        likeRepository.removeLike(filmId1, userId1);

        List<Like> likes = likeRepository.findLikesByFilmId(filmId1);
        assertThat(likes).isEmpty();
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при удалении несуществующего лайка")
    void shouldThrowWhenRemovingNonExistingLike() {
        assertThatThrownBy(() -> likeRepository.removeLike(filmId1, userId1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь или фильм не найдены");
    }

    @Test
    @DisplayName("Должен возвращать список лайков по ID фильма")
    void shouldFindLikesByFilmId() {
        likeRepository.addLike(filmId1, userId1);
        likeRepository.addLike(filmId1, userId2);

        List<Like> likes = likeRepository.findLikesByFilmId(filmId1);
        assertThat(likes)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(
                        new Like(filmId1, userId1),
                        new Like(filmId1, userId2)
                ));
    }

    @Test
    @DisplayName("Должен возвращать пустой список при отсутствии лайков")
    void shouldReturnEmptyListWhenNoLikes() {
        List<Like> likes = likeRepository.findLikesByFilmId(filmId1);
        assertThat(likes).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать количество лайков для фильма")
    void shouldGetLikesCountForFilm() {
        likeRepository.addLike(filmId1, userId1);
        likeRepository.addLike(filmId1, userId2);
        likeRepository.addLike(filmId2, userId1);

        Long count = likeRepository.getLikesCountForFilm(filmId1);
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("Должен возвращать 0 при отсутствии лайков")
    void shouldReturnZeroWhenNoLikes() {
        Long count = likeRepository.getLikesCountForFilm(filmId1);
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("Должен обрабатывать несколько лайков от разных пользователей")
    void shouldHandleMultipleLikes() {
        likeRepository.addLike(filmId1, userId1);
        likeRepository.addLike(filmId1, userId2);
        likeRepository.addLike(filmId2, userId1);

        assertThat(likeRepository.findLikesByFilmId(filmId1))
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(
                        new Like(filmId1, userId1),
                        new Like(filmId1, userId2)
                ));

        assertThat(likeRepository.findLikesByFilmId(filmId2))
                .usingRecursiveComparison()
                .isEqualTo(List.of(new Like(filmId2, userId1)));

        assertThat(likeRepository.getLikesCountForFilm(filmId1)).isEqualTo(2L);
        assertThat(likeRepository.getLikesCountForFilm(filmId2)).isEqualTo(1L);
    }

    @Test
    @DisplayName("Должен обрабатывать удаление лайка после добавления")
    void shouldHandleAddAndRemoveLike() {
        likeRepository.addLike(filmId1, userId1);
        likeRepository.addLike(filmId1, userId2);
        likeRepository.removeLike(filmId1, userId1);

        List<Like> likes = likeRepository.findLikesByFilmId(filmId1);
        assertThat(likes)
                .usingRecursiveComparison()
                .isEqualTo(List.of(new Like(filmId1, userId2)));

        assertThat(likeRepository.getLikesCountForFilm(filmId1)).isEqualTo(1L);
    }
}