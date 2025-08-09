package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JdbcTest
@Import(JdbcGenreRepository.class)
@DisplayName("JdbcGenreRepositoryTest")
class JdbcGenreRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcGenreRepository genreRepository;

    private Genre genre1;
    private Genre genre2;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM genre");
        jdbcTemplate.update("DELETE FROM films");

        jdbcTemplate.update("INSERT INTO genre (genre_id, genre_name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("INSERT INTO genre (genre_id, genre_name) VALUES (2, 'Драма')");
        jdbcTemplate.update("INSERT INTO genre (genre_id, genre_name) VALUES (3, 'Мультфильм')");

        jdbcTemplate.update("INSERT INTO films (film_id, films_name, description, release_date, duration, mpa_id) " +
                "VALUES (1, 'Test Film', 'Description', '2000-01-01', 120, 1)");

        genre1 = new Genre(1L, "Комедия");
        genre2 = new Genre(2L, "Драма");
        testFilm = new Film();
        testFilm.setId(1L);
    }

    @Test
    @DisplayName("Должен находить жанр по ID")
    void shouldFindGenreById() {
        Genre foundGenre = genreRepository.findById(1L);

        assertThat(foundGenre)
                .usingRecursiveComparison()
                .isEqualTo(genre1);
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при поиске несуществующего жанра")
    void shouldThrowWhenFindingNonExistingGenre() {
        assertThatThrownBy(() -> genreRepository.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Один или несколько жанров не найдены");
    }

    @Test
    @DisplayName("Должен возвращать все жанры")
    void shouldFindAllGenres() {
        Collection<Genre> genres = genreRepository.findAll();

        assertThat(genres)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(
                        new Genre(1L, "Комедия"),
                        new Genre(2L, "Драма"),
                        new Genre(3L, "Мультфильм")
                ));
    }

    @Test
    @DisplayName("Должен валидировать существующие жанры")
    void shouldValidateExistingGenres() {
        Set<Genre> genres = Set.of(genre1, genre2);
        genreRepository.validateGenres(genres);
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при валидации несуществующих жанров")
    void shouldThrowWhenValidatingNonExistingGenres() {
        Set<Genre> genres = Set.of(new Genre(999L, "Фантастика"));

        assertThatThrownBy(() -> genreRepository.validateGenres(genres))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Один или несколько жанров не существуют");
    }

    @Test
    @DisplayName("Должен добавлять жанр из ResultSet в фильм")
    void shouldAddGenreFromResultSet() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("genre_id")).thenReturn(1L);
        when(rs.wasNull()).thenReturn(false);
        when(rs.getString("genre_name")).thenReturn("Комедия");

        Film film = new Film();
        genreRepository.addGenreFromResultSet(rs, film);

        assertThat(film.getGenres())
                .usingRecursiveComparison()
                .isEqualTo(Set.of(genre1));
    }

    @Test
    @DisplayName("Должен сохранять жанры фильма")
    void shouldSaveFilmGenres() {
        testFilm.setGenres(Set.of(genre1, genre2));
        genreRepository.saveFilmGenres(testFilm);

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                "SELECT genre_id FROM film_genre WHERE film_id = ?",
                testFilm.getId()
        );

        Set<Long> savedGenreIds = new LinkedHashSet<>();
        while (rowSet.next()) {
            savedGenreIds.add(rowSet.getLong("genre_id"));
        }

        assertThat(savedGenreIds).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("Должен обновлять жанры фильма")
    void shouldUpdateFilmGenres() {
        testFilm.setGenres(Set.of(genre1));
        genreRepository.saveFilmGenres(testFilm);

        testFilm.setGenres(Set.of(genre2));
        genreRepository.updateFilmGenres(testFilm);

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                "SELECT genre_id FROM film_genre WHERE film_id = ?",
                testFilm.getId()
        );

        Set<Long> updatedGenreIds = new LinkedHashSet<>();
        while (rowSet.next()) {
            updatedGenreIds.add(rowSet.getLong("genre_id"));
        }

        assertThat(updatedGenreIds).containsExactly(2L);
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при сохранении невалидных жанров")
    void shouldThrowWhenSavingInvalidGenres() {
        testFilm.setGenres(Set.of(new Genre(999L, "Фантастика")));

        assertThatThrownBy(() -> genreRepository.saveFilmGenres(testFilm))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Один или несколько жанров не существуют");
    }
}