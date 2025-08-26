package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface FriendRepository {

    public void addFriend(long userId, long friendId);

    public void removeFriend(long userId, long friendId);

    public void updateFriends(User user);

    public boolean existsById(long userId);

    public void loadFriendsForUser(User user);

    public List<User> findFriendsByUserId(long userId);
}