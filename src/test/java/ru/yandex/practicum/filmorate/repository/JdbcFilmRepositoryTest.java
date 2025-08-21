package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({
        JdbcFilmRepository.class,
        JdbcMpaRepository.class,
        JdbcGenreRepository.class,
        JdbcLikeRepository.class,
        JdbcUserRepository.class,
        JdbcFriendRepository.class,
        JdbcDirectorRepository.class
})
@DisplayName("JdbcFilmRepositoryTest")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JdbcFilmRepositoryTest {
    private final JdbcFilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    private Film testFilm;

    @BeforeEach
    void setUp() {

        Mpa mpa = mpaRepository.findById(1L);

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2023, 1, 1));
        testFilm.setDuration(Duration.ofMinutes(120));
        testFilm.setMpa(mpa);
    }

    @Test
    @DisplayName("Should save and find film by id")
    void shouldSaveAndFindFilmById() {
        Film savedFilm = filmRepository.save(testFilm);
        Film foundFilm = filmRepository.getFilmById(savedFilm.getId());

        assertThat(foundFilm)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(testFilm);

        assertThat(foundFilm.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when film not found")
    void shouldThrowExceptionWhenFilmNotFound() {
        assertThatThrownBy(() -> filmRepository.getFilmById(9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм не найден");
    }

    @Test
    @DisplayName("Should update film")
    void shouldUpdateFilm() {
        Film savedFilm = filmRepository.save(testFilm);

        savedFilm.setName("Updated Name");
        savedFilm.setDescription("Updated Description");
        savedFilm.setReleaseDate(LocalDate.of(2024, 2, 2));
        savedFilm.setDuration(Duration.ofMinutes(150));

        Film updatedFilm = filmRepository.update(savedFilm);

        assertThat(updatedFilm)
                .usingRecursiveComparison()
                .isEqualTo(savedFilm);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent film")
    void shouldThrowExceptionWhenUpdatingNonExistentFilm() {
        testFilm.setId(9999L);
        assertThatThrownBy(() -> filmRepository.update(testFilm))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("Should delete film by id")
    void shouldDeleteFilmById() {
        Film savedFilm = filmRepository.save(testFilm);
        filmRepository.deleteById(savedFilm.getId());

        assertThatThrownBy(() -> filmRepository.getFilmById(savedFilm.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм не найден");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent film")
    void shouldThrowExceptionWhenDeletingNonExistentFilm() {
        assertThatThrownBy(() -> filmRepository.deleteById(9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("Should find all films")
    void shouldFindAllFilms() {
        filmRepository.save(testFilm);
        filmRepository.save(testFilm);

        Collection<Film> films = filmRepository.findAll();
        assertThat(films).hasSize(2);
    }

    @Test
    @DisplayName("Should find popular films ordered by likes")
    void shouldFindPopularFilmsOrderedByLikes() {
        Film film1 = filmRepository.save(testFilm);
        Film film2 = filmRepository.save(testFilm);

        User user1 = userRepository.save(createTestUser("user1@mail.com"));
        User user2 = userRepository.save(createTestUser("user2@mail.com"));

        likeRepository.addLike(film1.getId(), user1.getId());
        likeRepository.addLike(film1.getId(), user2.getId());
        likeRepository.addLike(film2.getId(), user1.getId());

        Collection<Film> popularFilms = filmRepository.findPopularFilms(2L);

        assertThat(popularFilms)
                .extracting(Film::getId)
                .containsExactly(film1.getId(), film2.getId());
    }

    @Test
    @DisplayName("Should return empty list when no films exist")
    void shouldReturnEmptyListWhenNoFilmsExist() {
        Collection<Film> films = filmRepository.findAll();
        assertThat(films).isEmpty();
    }

    private User createTestUser(String email) {
        User user = new User(email, "login", LocalDate.of(2000, 1, 1));
        user.setName("name");
        return user;
    }
}