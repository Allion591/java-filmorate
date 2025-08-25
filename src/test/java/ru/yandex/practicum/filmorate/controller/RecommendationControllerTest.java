package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Mpa mpa;
    private Set<Genre> genres;

    @BeforeEach
    void init() {
        mpa = new Mpa(1, "G");
        Genre comedy = new Genre(1, "Комедия");
        genres = new LinkedHashSet<>();
        genres.add(comedy);
    }

    @Test
    void recommendations_ReturnsEmpty_WhenTargetUserHasNoLikes() throws Exception {
        long u1 = createUser(new User("u1@mail.com", "u1login", LocalDate.of(1990,1,1)));
        mockMvc.perform(get("/users/{id}/recommendations", u1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void recommendations_ReturnsEmpty_WhenNoOverlapUsers() throws Exception {
        long u1 = createUser(new User("u1@mail.com", "u1login", LocalDate.of(1990,1,1)));
        long u2 = createUser(new User("u2@mail.com", "u2login", LocalDate.of(1991,1,1)));

        long f1 = createFilm(makeFilm("F1", LocalDate.of(2000,1,1)));
        long f2 = createFilm(makeFilm("F2", LocalDate.of(2001,1,1)));

        likeFilm(f1, u1);
        likeFilm(f2, u2);

        mockMvc.perform(get("/users/{id}/recommendations", u1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void recommendations_ReturnsSortedCandidates_FromBestSimilarUser() throws Exception {
        long u1 = createUser(new User("u1@mail.com", "u1login", LocalDate.of(1990,1,1)));
        long u2 = createUser(new User("u2@mail.com", "u2login", LocalDate.of(1991,1,1)));
        long u3 = createUser(new User("u3@mail.com", "u3login", LocalDate.of(1992,1,1)));

        long f1 = createFilm(makeFilm("F1", LocalDate.of(2000,1,1)));
        long f2 = createFilm(makeFilm("F2", LocalDate.of(2001,1,1)));
        long f3 = createFilm(makeFilm("F3", LocalDate.of(2002,1,1)));
        long f5 = createFilm(makeFilm("F5", LocalDate.of(2005,1,1)));

        likeFilm(f1, u1);
        likeFilm(f2, u1);

        likeFilm(f1, u2);
        likeFilm(f2, u2);
        likeFilm(f3, u2);
        likeFilm(f5, u2);

        likeFilm(f1, u3);
        likeFilm(f5, u3);

        mockMvc.perform(get("/users/{id}/recommendations", u1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(f5))
                .andExpect(jsonPath("$[1].id").value(f3));
    }

    private Film makeFilm(String name, LocalDate release) {
        Film f = new Film();
        f.setName(name);
        f.setDescription("desc " + name);
        f.setReleaseDate(release);
        f.setDuration(Duration.ofMinutes(100));
        f.setMpa(mpa);
        f.setGenres(genres);
        return f;
    }

    private long createUser(User u) throws Exception {
        u.setName(u.getLogin());
        MvcResult res = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u)))
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private long createFilm(Film f) throws Exception {
        MvcResult res = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(f)))
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private void likeFilm(long filmId, long userId) throws Exception {
        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }
}
