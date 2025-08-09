package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.FriendRepository;
import ru.yandex.practicum.filmorate.interfaces.UserRepository;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@Import({
        JdbcUserRepository.class,
        JdbcFriendRepository.class
})
@DisplayName("JdbcUserRepositoryTest")
public class JdbcUserRepositoryTest {

    @Autowired
    @Qualifier("jdbcUserRepository")
    private UserRepository userRepository;

    @Autowired
    @Qualifier("jdbcFriendRepository")
    private FriendRepository friendRepository;

    private User testUser;
    private User secondUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@email.com", "testLogin", LocalDate.of(1990, 1, 1));
        testUser.setName("Test User");

        secondUser = new User("second@email.com", "secondLogin",
                LocalDate.of(1995, 5, 5));
        secondUser.setName("Second User");
    }

    @Test
    @DisplayName("save Should Save User With Generated Id")
    void save_shouldSaveUserWithGeneratedId() {
        User savedUser = userRepository.save(testUser);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isPositive();

        User retrievedUser = userRepository.getUserById(savedUser.getId());
        assertThat(retrievedUser)
                .usingRecursiveComparison()
                .ignoringFields("friends")
                .isEqualTo(savedUser);
    }

    @Test
    @DisplayName("update Should Update User Data")
    void update_shouldUpdateUserData() {
        User savedUser = userRepository.save(testUser);
        savedUser.setEmail("updated@email.com");
        savedUser.setLogin("updatedLogin");
        savedUser.setName("Updated Name");
        savedUser.setBirthday(LocalDate.of(2000, 12, 31));

        User updatedUser = userRepository.update(savedUser);

        User retrievedUser = userRepository.getUserById(updatedUser.getId());
        assertThat(retrievedUser)
                .usingRecursiveComparison()
                .ignoringFields("friends")
                .isEqualTo(updatedUser);
    }

    @Test
    @DisplayName("update Should Throw NotFoundException")
    void update_shouldThrowNotFoundException() {
        User nonExistentUser = new User("ghost@email.com", "ghost", LocalDate.now());
        nonExistentUser.setId(9999L);

        assertThatThrownBy(() -> userRepository.update(nonExistentUser))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("delete should RemoveUser")
    void delete_shouldRemoveUser() {
        User savedUser = userRepository.save(testUser);

        String result = userRepository.delete(savedUser);

        assertThat(result).isEqualTo("Пользователь " + savedUser.getId() + " удалён");
        assertThatThrownBy(() -> userRepository.getUserById(savedUser.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("findAll should Return All Users")
    void findAll_shouldReturnAllUsers() {
        User user1 = userRepository.save(testUser);
        User user2 = userRepository.save(secondUser);

        Collection<User> users = userRepository.findAll();

        assertThat(users)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("friends")
                .containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    @DisplayName("getUserById should Return Correct User")
    void getUserById_shouldReturnCorrectUser() {
        User savedUser = userRepository.save(testUser);

        User retrievedUser = userRepository.getUserById(savedUser.getId());

        assertThat(retrievedUser)
                .usingRecursiveComparison()
                .ignoringFields("friends")
                .isEqualTo(savedUser);
    }

    @Test
    @DisplayName("getUserById should Throw Not Found Exception")
    void getUserById_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> userRepository.getUserById(9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID 9999 не найден");
    }

    @Test
    @DisplayName("find CommonFriends should Return Mutual Friends")
    void findCommonFriends_shouldReturnMutualFriends() {
        User user1 = userRepository.save(testUser);
        User user2 = userRepository.save(secondUser);
        User commonFriend = new User("friend@email.com", "friend",
                LocalDate.of(2000, 5, 5));
        commonFriend.setName("commonFriendName");
        userRepository.save(commonFriend);

        friendRepository.addFriend(user1.getId(), commonFriend.getId());
        friendRepository.addFriend(user2.getId(), commonFriend.getId());

        Collection<User> commonFriends = userRepository.findCommonFriends(
                user1.getId(),
                user2.getId()
        );

        assertThat(commonFriends)
                .hasSize(1)
                .first()
                .usingRecursiveComparison()
                .ignoringFields("friends")
                .isEqualTo(commonFriend);
    }

    @Test
    @DisplayName("loadFriends should Populate Friends Set")
    void loadFriends_shouldPopulateFriendsSet() {
        User user1 = userRepository.save(testUser);
        User user2 = userRepository.save(secondUser);
        friendRepository.addFriend(user1.getId(), user2.getId());

        user1 = userRepository.getUserById(user1.getId());

        assertThat(user1.getFriends())
                .containsExactly(user2.getId());
    }
}