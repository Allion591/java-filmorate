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
        Mpa mpa = new Mpa(1, "G");
        Genre comedy = new Genre(1, "Комедия");

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
        // set required MPA so repository.save() doesn't fail
        anotherFilm.setMpa(new Mpa(1, "G"));
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

        // Создаем пользователя и получаем его ID
        MvcResult userResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andReturn();
        JsonNode userJson = objectMapper.readTree(userResult.getResponse().getContentAsString());
        Long userId = userJson.get("id").asLong();

        // Ставим лайк
        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }

    @Test
    void addLike_InvalidFilmOrUser() throws Exception {
        mockMvc.perform(put("/films/999/like/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Пользователь или фильм не найдены"));
    }

    @Test
    void removeLike_ValidLike() throws Exception {
        // Создаем фильм и получаем его ID
        MvcResult filmResult = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn();
        JsonNode filmJson = objectMapper.readTree(filmResult.getResponse().getContentAsString());
        Long filmId = filmJson.get("id").asLong();

        // Создаем пользователя и получаем его ID
        MvcResult userResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user3)))
                .andReturn();
        JsonNode userJson = objectMapper.readTree(userResult.getResponse().getContentAsString());
        Long userId = userJson.get("id").asLong();

        // Ставим лайк
        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        // Удаляем лайк
        mockMvc.perform(delete("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Лайк удален"));
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
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Valid Film"));
    }

    @Test
    void getPopular_NegativeCount() throws Exception {
        mockMvc.perform(get("/films/popular?count=-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Количество лайков не может быть отрицательным"));
    }
}