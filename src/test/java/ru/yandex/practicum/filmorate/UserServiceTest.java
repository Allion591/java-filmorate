package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFriendException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private final UserStorage inMemoryUserStorage = new InMemoryUserStorage();
    private final UserService userService = new UserService(inMemoryUserStorage);

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = new User("valid@email.com", "validLogin",
                LocalDate.of(2001, 3, 9));
        userService.create(user1);

        user2 = new User("valid@email.com", "validLogin",
                LocalDate.of(2002, 2, 6));
        userService.create(user2);

        user3 = new User("valid@email.com", "validLogin",
                LocalDate.of(2003, 1, 3));
        userService.create(user3);
    }

    @Test
    void shouAddFriend() {
        userService.addFriend(user1.getId(), user2.getId());
        List<Long> list1 = new ArrayList<>(user2.getFriends());
        List<Long> list2 = new ArrayList<>(user1.getFriends());

        assertEquals(user1.getId(), list1.getFirst());
        assertEquals(user2.getId(), list2.getFirst());
    }

    @Test
    void showRemoveFriend() {
        userService.addFriend(user1.getId(), user2.getId());
        List<Long> list1 = new ArrayList<>(user2.getFriends());
        List<Long> list2 = new ArrayList<>(user1.getFriends());

        assertEquals(user1.getId(), list1.getFirst());
        assertEquals(user2.getId(), list2.getFirst());

        userService.removeFriend(user1.getId(), user2.getId());

        List<Long> list3 = new ArrayList<>(user2.getFriends());
        List<Long> list4 = new ArrayList<>(user1.getFriends());

        assertTrue(list3.isEmpty());
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
        userService.addFriend(user1.getId(), user3.getId());

        List<User> list1 = new ArrayList<>(userService.getMutualFriends(user2.getId(), user3.getId()));

        assertEquals(user1.getId(), list1.get(0).getId());
    }
}