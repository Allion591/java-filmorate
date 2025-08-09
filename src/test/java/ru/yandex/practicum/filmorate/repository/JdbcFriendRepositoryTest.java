package ru.yandex.practicum.filmorate.repository;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotFriendException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({JdbcFriendRepository.class,
        JdbcUserRepository.class
})
@DisplayName("JdbcFriendRepositoryTest")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JdbcFriendRepositoryTest {
    private final JdbcFriendRepository friendRepository;
    private final JdbcUserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = createTestUser("user1@mail.com");
        user2 = createTestUser("user2@mail.com");
        user3 = createTestUser("user3@mail.com");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
    }

    @Test
    @DisplayName("Должен добавлять друга")
    void shouldAddFriend() {
        friendRepository.addFriend(user1.getId(), user2.getId());

        List<User> friends = friendRepository.findFriendsByUserId(user1.getId());
        assertThat(friends)
                .usingRecursiveComparison()
                .ignoringFields("friends")
                .isEqualTo(List.of(user2));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при добавлении несуществующего пользователя")
    void shouldThrowWhenAddingNonExistingUser() {
        assertThatThrownBy(() -> friendRepository.addFriend(9999L, user2.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Один из пользователей не найден");
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при повторном добавлении друга")
    void shouldThrowWhenAddingDuplicateFriend() {
        friendRepository.addFriend(user1.getId(), user2.getId());

        assertThatThrownBy(() -> friendRepository.addFriend(user1.getId(), user2.getId()))
                .isInstanceOf(DuplicateRequestException.class)
                .hasMessageContaining("Заявка в друзья уже отправлена");
    }

    @Test
    @DisplayName("Должен удалять друга")
    void shouldRemoveFriend() {
        friendRepository.addFriend(user1.getId(), user2.getId());
        friendRepository.removeFriend(user1.getId(), user2.getId());

        List<User> friends = friendRepository.findFriendsByUserId(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при удалении несуществующей дружбы")
    void shouldThrowWhenRemovingNonExistingFriend() {
        assertThatThrownBy(() -> friendRepository.removeFriend(user1.getId(), user2.getId()))
                .isInstanceOf(NotFriendException.class)
                .hasMessageContaining("Пользователи не являются друзьями");
    }

    @Test
    @DisplayName("Должен возвращать список друзей")
    void shouldFindFriendsByUserId() {
        friendRepository.addFriend(user1.getId(), user2.getId());
        friendRepository.addFriend(user1.getId(), user3.getId());

        List<User> friends = friendRepository.findFriendsByUserId(user1.getId());
        assertThat(friends)
                .usingRecursiveComparison()
                .ignoringFields("friends")
                .ignoringCollectionOrder()
                .isEqualTo(List.of(user2, user3));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при поиске друзей несуществующего пользователя")
    void shouldThrowWhenFindingFriendsOfNonExistingUser() {
        assertThatThrownBy(() -> friendRepository.findFriendsByUserId(9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Один из пользователей не найден");
    }

    @Test
    @DisplayName("Должен загружать друзей для пользователя")
    void shouldLoadFriendsForUser() {
        friendRepository.addFriend(user1.getId(), user2.getId());
        friendRepository.addFriend(user1.getId(), user3.getId());

        User user = new User("test@mail.com", "test", LocalDate.now());
        user.setId(user1.getId());

        friendRepository.loadFriendsForUser(user);

        assertThat(user.getFriends())
                .isEqualTo(Set.of(user2.getId(), user3.getId()));
    }

    @Test
    @DisplayName("Должен обновлять список друзей")
    void shouldUpdateFriends() {
        friendRepository.addFriend(user1.getId(), user2.getId());
        user1.getFriends().add(user3.getId());

        friendRepository.updateFriends(user1);

        List<User> friends = friendRepository.findFriendsByUserId(user1.getId());
        assertThat(friends)
                .usingRecursiveComparison()
                .ignoringFields("friends")
                .isEqualTo(List.of(user3));
    }

    @Test
    @DisplayName("Должен проверять существование пользователя")
    void shouldCheckIfUserExists() {
        assertThat(friendRepository.existsById(user1.getId())).isTrue();
        assertThat(friendRepository.existsById(9999L)).isFalse();
    }

    private User createTestUser(String email) {
        User user = new User(email, email.split("@")[0], LocalDate.of(2000, 1, 1));
        user.setName("Test User");
        return user;
    }
}