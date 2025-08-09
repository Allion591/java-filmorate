package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private User validUser;
    private User validUser2;
    private User userWithEmptyName;
    private User invalidEmailUser;
    private User invalidLoginUser;
    private User futureBirthdayUser;

    @BeforeEach
    void setUp() {
        validUser = new User("valid@email.ru", "validLogin",
                LocalDate.of(1990, 5, 15));
        validUser.setName("ValidName");

        validUser2 = new User("valid@email2.ru", "validLogin2",
                LocalDate.of(1993, 1, 19));
        validUser2.setName("ValidName2");

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
                .andExpect(jsonPath("$.name").value(""));
    }

    @Test
    void createUser_InvalidEmail_Returns400() throws Exception {
        String userJson = objectMapper.writeValueAsString(invalidEmailUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
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
        nonExistentUser.setId(9999L);
        nonExistentUser.setName("Ghost");

        String userJson = objectMapper.writeValueAsString(nonExistentUser);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_ReturnsUsersList() throws Exception {

        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty());
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

    @Test
    void getUserById_ValidId_Returns200() throws Exception {
        long userId = createUserViaApi(validUser);

        mockMvc.perform(get("/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("ValidName"));
    }

    @Test
    void getUserById_InvalidId_Returns404() throws Exception {
        mockMvc.perform(get("/users/{id}", 9999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void addFriend_ValidIds_Returns200() throws Exception {
        long user1Id = createUserViaApi(validUser);
        User friend = new User("friend@mail.ru", "friendLogin",
                LocalDate.of(1995, 3, 10));
        friend.setName("Friend");
        long user2Id = createUserViaApi(friend);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user1Id, user2Id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Пользователь с ид " + user1Id +
                        " и пользователь с ид " + user2Id + " теперь друзья!"));
    }

    @Test
    void addFriend_NonExistentUser_Returns404() throws Exception {
        long existingId = createUserViaApi(validUser);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", existingId, 9999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFriend_ValidIds_Returns200() throws Exception {
        long user1Id = createUserViaApi(validUser);
        User friend = new User("friend@mail.ru", "friendLogin",
                LocalDate.of(1995, 3, 10));
        friend.setName("Friend");
        long user2Id = createUserViaApi(friend);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user1Id, user2Id))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", user1Id, user2Id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Пользователь c ID" + user2Id + " теперь вам не друг"));
    }

    @Test
    void removeFriend_NotFriends_Returns404() throws Exception {
        long user1Id = createUserViaApi(validUser);
        long user2Id = 999L;

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", user1Id, user2Id))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getFriends_ValidUser_ReturnsFriendsList() throws Exception {
        long userId = createUserViaApi(validUser);
        User friend1 = new User("friend1@mail.ru", "friend1",
                LocalDate.of(1985, 7, 12));
        friend1.setName("friend1");
        User friend2 = new User("friend2@mail.ru", "friend2",
                LocalDate.of(1992, 11, 3));
        friend2.setName("friend2");
        long friend1Id = createUserViaApi(friend1);
        long friend2Id = createUserViaApi(friend2);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId, friend1Id))
                .andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId, friend2Id))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(friend1Id))
                .andExpect(jsonPath("$[1].id").value(friend2Id));
    }

    @Test
    void getMutualFriends_ValidUsers_ReturnsCommonFriends() throws Exception {
        long user1Id = createUserViaApi(validUser);
        long user2Id = createUserViaApi(validUser2);

        User commonFriend = new User("common@friend.ru", "commonFriend",
                LocalDate.of(2000, 1, 1));
        commonFriend.setName("common");
        long commonId = createUserViaApi(commonFriend);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user1Id, commonId))
                .andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", user2Id, commonId))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", user1Id, user2Id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(commonId));
    }
}