package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface FriendRepository {

    public void addFriend(Long userId, Long friendId);

    public void removeFriend(Long userId, Long friendId);

    public void updateFriends(User user);

    public boolean existsById(Long userId);

    public void loadFriendsForUser(User user);

    public List<User> findFriendsByUserId(Long userId);
}