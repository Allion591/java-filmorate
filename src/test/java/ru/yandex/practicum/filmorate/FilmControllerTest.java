package ru.yandex.practicum.filmorate;

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
import ru.yandex.practicum.filmorate.model.User;


import java.time.Duration;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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

    @BeforeEach
    void setUp() {
        user = new User("user@mail.ru", "user_login", LocalDate.of(1990, 1, 1));

        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Normal description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(Duration.ofMinutes(120));

        validFilm2 = new Film();
        validFilm2.setName("Valid Film2");
        validFilm2.setDescription("Normal description");
        validFilm2.setReleaseDate(LocalDate.of(2010, 7, 1));
        validFilm2.setDuration(Duration.ofMinutes(180));

        filmWithEmptyName = new Film();
        filmWithEmptyName.setName("");
        filmWithEmptyName.setDescription("Description");
        filmWithEmptyName.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmWithEmptyName.setDuration(Duration.ofMinutes(120));

        invalidDescriptionLengthFilm = new Film();
        invalidDescriptionLengthFilm.setName("Film");
        invalidDescriptionLengthFilm.setDescription("a".repeat(201));
        invalidDescriptionLengthFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidDescriptionLengthFilm.setDuration(Duration.ofMinutes(120));

        filmWithInvalidReleaseDate = new Film();
        filmWithInvalidReleaseDate.setName("Old Film");
        filmWithInvalidReleaseDate.setDescription("Description");
        filmWithInvalidReleaseDate.setReleaseDate(LocalDate.of(1895, 12, 27));
        filmWithInvalidReleaseDate.setDuration(Duration.ofMinutes(120));

        filmWithNegativeDuration = new Film();
        filmWithNegativeDuration.setName("Negative Duration");
        filmWithNegativeDuration.setDescription("Description");
        filmWithNegativeDuration.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmWithNegativeDuration.setDuration(Duration.ofMinutes(-5));
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
                .andExpect(jsonPath("$.message")
                        .value("Фильм с названием = Non-existent Film не найден"));
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
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(anotherFilm)));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name").value("Valid Film"))
                .andExpect(jsonPath("$[1].name").value("Valid Film2"));
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
                .andExpect(jsonPath("$.message")
                        .value("Фильм не найден"));
    }

    @Test
    void addLike_ValidFilmAndUser() throws Exception {

        MvcResult filmResult = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn();
        Film film = objectMapper.readValue(filmResult.getResponse().getContentAsString(), Film.class);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andReturn();


        mockMvc.perform(put("/films/{id}/like/{userId}", film.getId(), 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Спасибо за оценку."));
    }

    @Test
    void addLike_InvalidFilmOrUser() throws Exception {
        mockMvc.perform(put("/films/999/like/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Пользователь не найден"));
    }

    @Test
    void removeLike_ValidLike() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andReturn();


        mockMvc.perform(put("/films/{id}/like/{userId}", 1, 1));


        mockMvc.perform(delete("/films/{id}/like/{userId}", 1, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Лайк удален"));
    }

    @Test
    void removeLike_InvalidLike() throws Exception {
        mockMvc.perform(delete("/films/999/like/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Фильм не найден"));
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
}