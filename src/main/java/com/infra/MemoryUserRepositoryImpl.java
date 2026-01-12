package com.infra;

import com.core.model.User;
import com.core.port.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryUserRepositoryImpl implements UserRepository {
    private Map<String, User> userMap = new ConcurrentHashMap<>();

    @Override
    public void save(String username, User user) {
        if (username == null || user == null) {
            throw new IllegalArgumentException("Имя пользователя и пользователь не могут быть null");
        }
        userMap.put(username, user);
    }

    @Override
    public User find(String username) {
        return userMap.get(username);
    }

    @Override
    public void delete(String username) {
        if (username != null) {
            userMap.remove(username);
        }
    }

    @Override
    public Map<String, User> findAll() {
        return new HashMap<>(userMap); // Возвращаем копию для безопасности
    }

    @Override
    public void setAllUsers(Map<String, User> users) {
        if (users == null) {
            this.userMap = new ConcurrentHashMap<>();
        } else {
            this.userMap = new ConcurrentHashMap<>(users);
        }
    }

    @Override
    public boolean containsUser(String username) {
        return username != null && userMap.containsKey(username);
    }

    public int getUserCount() {
        return userMap.size();
    }

    public void clear() {
        userMap.clear();
    }
}