package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.Film;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private Film validFilm;
    private Film validFilm2;
    private Film filmWithEmptyName;
    private Film invalidDescriptionLengthFilm;
    private Film filmWithInvalidReleaseDate;
    private Film filmWithNegativeDuration;
    private User user;
    private User user2;
    private User user3;


    @BeforeEach
    void setUp() {
        Mpa mpa = new Mpa(1L, "G");
        Genre comedy = new Genre(1L, "Комедия");

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(comedy);

        user = new User("user@mail4.ru", "user_login4", LocalDate.of(1990, 1, 1));
        user.setName("TestName4");

        user2 = new User("user@mail6.ru", "user_login6", LocalDate.of(1991, 1, 1));
        user2.setName("TestName6");

        user3 = new User("user@mail7.ru", "user_login7", LocalDate.of(1992, 1, 1));
        user3.setName("TestName7");

        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Normal description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(Duration.ofMinutes(120));
        validFilm.setMpa(mpa);
        validFilm.setGenres(genres);

        validFilm2 = new Film();
        validFilm2.setName("Valid Film2");
        validFilm2.setDescription("Normal description");
        validFilm2.setReleaseDate(LocalDate.of(2010, 7, 1));
        validFilm2.setDuration(Duration.ofMinutes(180));
        validFilm2.setMpa(mpa);
        validFilm2.setGenres(genres);

        filmWithEmptyName = new Film();
        filmWithEmptyName.setName("");
        filmWithEmptyName.setDescription("Description");
        filmWithEmptyName.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmWithEmptyName.setDuration(Duration.ofMinutes(120));
        filmWithEmptyName.setMpa(mpa);
        filmWithEmptyName.setGenres(genres);

        invalidDescriptionLengthFilm = new Film();
        invalidDescriptionLengthFilm.setName("Film");
        invalidDescriptionLengthFilm.setDescription("a".repeat(201));
        invalidDescriptionLengthFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidDescriptionLengthFilm.setDuration(Duration.ofMinutes(120));
        invalidDescriptionLengthFilm.setMpa(mpa);
        invalidDescriptionLengthFilm.setGenres(genres);

        filmWithInvalidReleaseDate = new Film();
        filmWithInvalidReleaseDate.setName("Old Film");
        filmWithInvalidReleaseDate.setDescription("Description");
        filmWithInvalidReleaseDate.setReleaseDate(LocalDate.of(1895, 12, 27));
        filmWithInvalidReleaseDate.setDuration(Duration.ofMinutes(120));
        filmWithInvalidReleaseDate.setMpa(mpa);
        filmWithInvalidReleaseDate.setGenres(genres);

        filmWithNegativeDuration = new Film();
        filmWithNegativeDuration.setName("Negative Duration");
        filmWithNegativeDuration.setDescription("Description");
        filmWithNegativeDuration.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmWithNegativeDuration.setDuration(Duration.ofMinutes(-5));
        filmWithNegativeDuration.setMpa(mpa);
        filmWithNegativeDuration.setGenres(genres);
    }

    @Test
    void createValidFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Valid Film"));
    }

    @Test
    void createFilmWithEmptyName() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithEmptyName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название фильма не может быть пустым"));
    }

    @Test
    void createFilmWithLongDescription() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDescriptionLengthFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description")
                        .value("Максимальная длина описания - 200 символов"));
    }

    @Test
    void createFilmWithInvalidReleaseDate() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithInvalidReleaseDate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.releaseDate")
                        .value("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    void createFilmWithNegativeDuration() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithNegativeDuration)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.durationPositive")
                        .value("Продолжительность должна быть положительной"));
    }

    @Test
    void updateValidFilm() throws Exception {

        MvcResult result = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn();

        Film createdFilm = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Film.class
        );

        createdFilm.setName("Updated Name");
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateNonExistentFilm() throws Exception {

        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999L);
        nonExistentFilm.setName("Non-existent Film");
        nonExistentFilm.setDescription("Valid description");
        nonExistentFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        nonExistentFilm.setDuration(Duration.ofMinutes(120));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentFilm)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Фильм с ID=999 не найден"));
    }

    @Test
    void getAllFilms() throws Exception {

        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm)));

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another description");
        anotherFilm.setReleaseDate(LocalDate.of(2010, 5, 15));
        anotherFilm.setDuration(Duration.ofMinutes(90));
        anotherFilm.setMpa(new Mpa(1L, "G"));
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(anotherFilm)));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Valid Film"));
    }

    @Test
    void getFilmById_ValidId() throws Exception {
        MvcResult result = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn();

        Film createdFilm = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Film.class
        );

        mockMvc.perform(get("/films/{id}", createdFilm.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdFilm.getId()))
                .andExpect(jsonPath("$.name").value("Valid Film"));
    }

    @Test
    void getFilmById_InvalidId() throws Exception {
        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Фильм не найден"));
    }

    @Test
    void addLike_ValidFilmAndUser() throws Exception {

        MvcResult filmResult = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn();
        JsonNode filmJson = objectMapper.readTree(filmResult.getResponse().getContentAsString());
        Long filmId = filmJson.get("id").asLong();

        MvcResult userResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andReturn();
        JsonNode userJson = objectMapper.readTree(userResult.getResponse().getContentAsString());
        Long userId = userJson.get("id").asLong();

        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void addLike_InvalidFilmOrUser() throws Exception {
        mockMvc.perform(put("/films/999/like/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Пользователь с ID 999 не найден"));
    }

    @Test
    void removeLike_ValidLike() throws Exception {
        MvcResult filmResult = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn();
        JsonNode filmJson = objectMapper.readTree(filmResult.getResponse().getContentAsString());
        Long filmId = filmJson.get("id").asLong();

        MvcResult userResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user3)))
                .andReturn();
        JsonNode userJson = objectMapper.readTree(userResult.getResponse().getContentAsString());
        Long userId = userJson.get("id").asLong();

        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }

    @Test
    void removeLike_InvalidLike() throws Exception {
        mockMvc.perform(delete("/films/999/like/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Пользователь или фильм не найдены"));
    }

    @Test
    void getPopular_ValidCount() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andReturn();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm2)))
                .andReturn();

        mockMvc.perform(put("/films/{id}/like/{userId}", 1L, 1L));

        mockMvc.perform(get("/films/popular?count=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Valid Film"));
    }

    @Test
    void getPopular_NegativeCount() throws Exception {
        mockMvc.perform(get("/films/popular?count=-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Количество лайков не может быть отрицательным"));
    }

    @Test
    void getPopular_FilterByGenre_ReturnsOnlySpecifiedGenre() throws Exception {
        JsonNode userNode = objectMapper.readTree(
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user)))
                        .andReturn().getResponse().getContentAsString());
        Long userId = userNode.get("id").asLong();

        Film comedyFilm = new Film();
        comedyFilm.setName("Comedy");
        comedyFilm.setDescription("desc");
        comedyFilm.setReleaseDate(LocalDate.of(2010, 1, 1));
        comedyFilm.setDuration(Duration.ofMinutes(100));
        comedyFilm.setMpa(new Mpa(1L, "G"));
        Set<Genre> comedyGenres = new LinkedHashSet<>();
        comedyGenres.add(new Genre(1L, "Комедия"));
        comedyFilm.setGenres(comedyGenres);

        JsonNode comedyNode = objectMapper.readTree(
                mockMvc.perform(post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(comedyFilm)))
                        .andReturn().getResponse().getContentAsString());
        Long comedyFilmId = comedyNode.get("id").asLong();

        Film dramaFilm = new Film();
        dramaFilm.setName("Drama");
        dramaFilm.setDescription("desc");
        dramaFilm.setReleaseDate(LocalDate.of(2010, 1, 1));
        dramaFilm.setDuration(Duration.ofMinutes(110));
        dramaFilm.setMpa(new Mpa(1L, "G"));
        Set<Genre> dramaGenres = new LinkedHashSet<>();
        dramaGenres.add(new Genre(2L, "Драма"));
        dramaFilm.setGenres(dramaGenres);

        JsonNode dramaNode = objectMapper.readTree(
                mockMvc.perform(post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dramaFilm)))
                        .andReturn().getResponse().getContentAsString());
        Long dramaFilmId = dramaNode.get("id").asLong();

        mockMvc.perform(put("/films/{id}/like/{userId}", comedyFilmId, userId))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/films/{id}/like/{userId}", dramaFilmId, userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/popular").param("genreId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genres[0].id").value(1));
    }

    @Test
    void getPopular_FilterByYear_ReturnsOnlySpecifiedYear() throws Exception {
        JsonNode userNode = objectMapper.readTree(
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user2)))
                        .andReturn().getResponse().getContentAsString());
        Long userId = userNode.get("id").asLong();

        Film film2009 = new Film();
        film2009.setName("F2009");
        film2009.setDescription("desc");
        film2009.setReleaseDate(LocalDate.of(2009, 6, 1));
        film2009.setDuration(Duration.ofMinutes(90));
        film2009.setMpa(new Mpa(1L, "G"));
        film2009.setGenres(new LinkedHashSet<>(Set.of(new Genre(1L, "Комедия"))));

        Long id2009 = objectMapper.readTree(
                mockMvc.perform(post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(film2009)))
                        .andReturn().getResponse().getContentAsString()).get("id").asLong();

        Film film2010 = new Film();
        film2010.setName("F2010");
        film2010.setDescription("desc");
        film2010.setReleaseDate(LocalDate.of(2010, 7, 1));
        film2010.setDuration(Duration.ofMinutes(95));
        film2010.setMpa(new Mpa(1L, "G"));
        film2010.setGenres(new LinkedHashSet<>(Set.of(new Genre(1L, "Комедия"))));

        Long id2010 = objectMapper.readTree(
                mockMvc.perform(post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(film2010)))
                        .andReturn().getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/films/{id}/like/{userId}", id2009, userId)).andExpect(status().isNoContent());
        mockMvc.perform(put("/films/{id}/like/{userId}", id2010, userId)).andExpect(status().isNoContent());

        mockMvc.perform(get("/films/popular").param("year", "2010"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].releaseDate").value("2010-07-01"));
    }

    @Test
    void getPopular_FilterByGenreAndYear_OrderingByLikes() throws Exception {
        Long u1 = objectMapper.readTree(
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user)))
                        .andReturn().getResponse().getContentAsString()).get("id").asLong();
        Long u2 = objectMapper.readTree(
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user2)))
                        .andReturn().getResponse().getContentAsString()).get("id").asLong();

        Film filmA = new Film();
        filmA.setName("A");
        filmA.setDescription("desc");
        filmA.setReleaseDate(LocalDate.of(2010, 1, 1));
        filmA.setDuration(Duration.ofMinutes(100));
        filmA.setMpa(new Mpa(1L, "G"));
        filmA.setGenres(new LinkedHashSet<>(Set.of(new Genre(1L, "Комедия"))));
        Long idA = objectMapper.readTree(
                mockMvc.perform(post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(filmA)))
                        .andReturn().getResponse().getContentAsString()).get("id").asLong();

        Film filmB = new Film();
        filmB.setName("B");
        filmB.setDescription("desc");
        filmB.setReleaseDate(LocalDate.of(2010, 5, 1));
        filmB.setDuration(Duration.ofMinutes(100));
        filmB.setMpa(new Mpa(1L, "G"));
        filmB.setGenres(new LinkedHashSet<>(Set.of(new Genre(1L, "Комедия"))));
        Long idB = objectMapper.readTree(
                mockMvc.perform(post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(filmB)))
                        .andReturn().getResponse().getContentAsString()).get("id").asLong();

        Film filmC = new Film();
        filmC.setName("C");
        filmC.setDescription("desc");
        filmC.setReleaseDate(LocalDate.of(2010, 6, 1));
        filmC.setDuration(Duration.ofMinutes(100));
        filmC.setMpa(new Mpa(1L, "G"));
        filmC.setGenres(new LinkedHashSet<>(Set.of(new Genre(2L, "Драма"))));
        Long idC = objectMapper.readTree(
                mockMvc.perform(post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(filmC)))
                        .andReturn().getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/films/{id}/like/{userId}", idA, u1)).andExpect(status().isNoContent());
        mockMvc.perform(put("/films/{id}/like/{userId}", idB, u1)).andExpect(status().isNoContent());
        mockMvc.perform(put("/films/{id}/like/{userId}", idB, u2)).andExpect(status().isNoContent());
        mockMvc.perform(put("/films/{id}/like/{userId}", idC, u1)).andExpect(status().isNoContent());
        mockMvc.perform(put("/films/{id}/like/{userId}", idC, u2)).andExpect(status().isNoContent());

        mockMvc.perform(get("/films/popular").param("genreId", "1").param("year", "2010").param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("B"))
                .andExpect(jsonPath("$[1].name").value("A"));
    }
}