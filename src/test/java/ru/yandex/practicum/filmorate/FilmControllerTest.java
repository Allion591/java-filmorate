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
    private Film filmWithEmptyName;
    private Film invalidDescriptionLengthFilm;
    private Film filmWithInvalidReleaseDate;
    private Film filmWithNegativeDuration;

    @BeforeEach
    void setUp() {

        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Normal description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(Duration.ofMinutes(120));

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
                .andExpect(status().isOk())
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
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Valid Film"))
                .andExpect(jsonPath("$[1].name").value("Another Film"));
    }
}