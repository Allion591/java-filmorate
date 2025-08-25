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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @Test
    @DisplayName("Should filter popular films by genre")
    void shouldFilterPopularFilmsByGenre() {
        // arrange: two films in different genres, both in same year
        Film comedy = createFilmWithGenreAndDate("Comedy", 1, LocalDate.of(2010, 1, 1));
        Film drama = createFilmWithGenreAndDate("Drama", 2, LocalDate.of(2010, 1, 1));
        Film s1 = filmRepository.save(comedy);
        Film s2 = filmRepository.save(drama);

        User u = userRepository.save(createTestUser("u1@mail.com"));
        likeRepository.addLike(s1.getId(), u.getId());
        likeRepository.addLike(s2.getId(), u.getId());

        Collection<Film> result = filmRepository.findPopular(10, 1, null);

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(f ->
                assertThat(f.getGenres().iterator().next().getId()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("Should filter popular films by year")
    void shouldFilterPopularFilmsByYear() {
        Film f2009 = createFilmWithGenreAndDate("F2009", 1, LocalDate.of(2009, 6, 1));
        Film f2010 = createFilmWithGenreAndDate("F2010", 1, LocalDate.of(2010, 7, 1));
        Film s1 = filmRepository.save(f2009);
        Film s2 = filmRepository.save(f2010);

        User u = userRepository.save(createTestUser("u2@mail.com"));
        likeRepository.addLike(s1.getId(), u.getId());
        likeRepository.addLike(s2.getId(), u.getId());

        Collection<Film> result = filmRepository.findPopular(10, null, 2010);

        assertThat(result)
                .extracting(Film::getReleaseDate)
                .allMatch(d -> d.getYear() == 2010);
    }

    @Test
    @DisplayName("Should filter by genre and year and order by likes desc then film_id")
    void shouldFilterByGenreAndYearAndOrderByLikes() {
        Film a = createFilmWithGenreAndDate("A", 1, LocalDate.of(2010, 1, 1));
        Film b = createFilmWithGenreAndDate("B", 1, LocalDate.of(2010, 5, 1));
        Film c = createFilmWithGenreAndDate("C", 2, LocalDate.of(2010, 6, 1));

        Film sa = filmRepository.save(a);
        Film sb = filmRepository.save(b);
        Film sc = filmRepository.save(c);

        User u1 = userRepository.save(createTestUser("u3@mail.com"));
        User u2 = userRepository.save(createTestUser("u4@mail.com"));

        likeRepository.addLike(sa.getId(), u1.getId());
        likeRepository.addLike(sb.getId(), u1.getId());
        likeRepository.addLike(sb.getId(), u2.getId());
        likeRepository.addLike(sc.getId(), u1.getId());
        likeRepository.addLike(sc.getId(), u2.getId());

        Collection<Film> result = filmRepository.findPopular(10, 1, 2010);

        assertThat(result)
                .extracting(Film::getName)
                .containsExactly("B", "A");
    }

    private Film createFilmWithGenreAndDate(String name, int genreId, LocalDate date) {
        Film f = new Film();
        f.setName(name);
        f.setDescription("desc");
        f.setReleaseDate(date);
        f.setDuration(Duration.ofMinutes(100));
        f.setMpa(mpaRepository.findById(1L));
        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(genreId, genreId == 1 ? "Комедия" : "Драма"));
        f.setGenres(genres);
        return f;
    }
}