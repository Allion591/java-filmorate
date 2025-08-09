package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFriendException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {
    private final UserService userService;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = new User("valid@email.com", "validLogin",
                LocalDate.of(2001, 3, 9));
        user1.setName("TestName1");
        userService.create(user1);

        user2 = new User("valid@email1.com", "validLogin1",
                LocalDate.of(2002, 2, 6));
        user2.setName("TestName2");
        userService.create(user2);

        user3 = new User("valid@email2.com", "validLogin2",
                LocalDate.of(2003, 1, 3));
        user3.setName("TestName3");
        userService.create(user3);
    }

    @Test
    void shouAddFriend() {
        userService.addFriend(user1.getId(), user2.getId());
        List<User> list1 = new ArrayList<>(userService.getFriends(user1.getId()));

        assertEquals(user2, list1.getFirst());
    }

    @Test
    void showRemoveFriend() {
        userService.addFriend(user1.getId(), user2.getId());
        List<User> list1 = new ArrayList<>(userService.getFriends(user1.getId()));

        assertEquals(user2.getId(), list1.getFirst().getId());

        userService.removeFriend(user1.getId(), user2.getId());

        List<Long> list4 = new ArrayList<>(user1.getFriends());

        assertTrue(list4.isEmpty());
    }

    @Test
    void showNotFriendRemove() {
        assertThrows(NotFriendException.class, () -> userService.removeFriend(user1.getId(), user3.getId()));
    }

    @Test
    void showFriendsByUser() {
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        List<User> list1 = new ArrayList<>(userService.getFriends(user1.getId()));

        assertEquals(user2.getId(), list1.get(0).getId());
        assertEquals(user3.getId(), list1.get(1).getId());
    }

    @Test
    void showMutualFriends() {
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user2.getId(), user1.getId());
        userService.addFriend(user1.getId(), user3.getId());
        userService.addFriend(user3.getId(), user1.getId());

        List<User> list1 = new ArrayList<>(userService.getMutualFriends(user2.getId(), user3.getId()));

        assertEquals(user1.getId(), list1.getFirst().getId());
    }
}