package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private User validUser;
    private User userWithEmptyName;
    private User invalidEmailUser;
    private User invalidLoginUser;
    private User futureBirthdayUser;

    @BeforeEach
    void setUp() {
        validUser = new User("valid@email.ru", "validLogin", LocalDate.of(1990, 5, 15));
        validUser.setName("ValidName");

        userWithEmptyName = new User("empty@name.ru", "emptyName",
                LocalDate.of(1990, 5, 15));
        userWithEmptyName.setName("");

        invalidEmailUser = new User("invalid-email", "invalidEmail",
                LocalDate.of(1990, 5, 15));
        invalidEmailUser.setName("InvalidEmail");

        invalidLoginUser = new User("invalid@login.ru", "",
                LocalDate.of(1990, 5, 15));
        invalidLoginUser.setName("InvalidLogin");

        futureBirthdayUser = new User("future@birthday.ru", "futureBirthday",
                LocalDate.now().plusDays(1));
        futureBirthdayUser.setName("Future");
    }

    @Test
    void createUser_ValidUser_Returns201() throws Exception {
        String userJson = objectMapper.writeValueAsString(validUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("ValidName"));
    }

    @Test
    void createUser_EmptyName_SetsNameAsLogin() throws Exception {
        String userJson = objectMapper.writeValueAsString(userWithEmptyName);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("emptyName")); // Проверка подстановки логина
    }

    @Test
    void createUser_InvalidEmail_Returns400() throws Exception {
        String userJson = objectMapper.writeValueAsString(invalidEmailUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isBadRequest()); // Ожидаем ошибку валидации
    }

    @Test
    void createUser_EmptyLogin_Returns400() throws Exception {
        String userJson = objectMapper.writeValueAsString(invalidLoginUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_FutureBirthday_Returns400() throws Exception {
        String userJson = objectMapper.writeValueAsString(futureBirthdayUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_ValidData_Returns200() throws Exception {
        long id1 = createUserViaApi(validUser);
        User updatedUser = new User("updated@email.ru", "updatedLogin",
                LocalDate.of(2000, 1, 1));
        updatedUser.setId(id1);
        updatedUser.setName("UpdatedName");

        String userJson = objectMapper.writeValueAsString(updatedUser);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.email").value("updated@email.ru"));
    }

    @Test
    void updateUser_NonExistentId_Returns404() throws Exception {
        User nonExistentUser = new User("ghost@user.ru", "ghost",
                LocalDate.of(1990, 1, 1));
        nonExistentUser.setId(9999L); // Несуществующий ID
        nonExistentUser.setName("Ghost");

        String userJson = objectMapper.writeValueAsString(nonExistentUser);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isNotFound()); // Ожидаем 404
    }

    @Test
    void getAllUsers_ReturnsUsersList() throws Exception {
        long id1 = createUserViaApi(validUser);
        long id2 = createUserViaApi(userWithEmptyName);

        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[1].id").value(id1))
                .andExpect(jsonPath("$[2].id").value(id2));
    }

    private long createUserViaApi(User user) throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}