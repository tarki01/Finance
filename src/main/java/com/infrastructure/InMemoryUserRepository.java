package com.infrastructure;

import com.business.entities.AccountHolder;
import com.business.ports.UserStoragePort;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserStoragePort {
    private Map<String, AccountHolder> userMap = new ConcurrentHashMap<>();

    @Override
    public void save(String username, AccountHolder accountHolder) {
        if (username == null || accountHolder == null) {
            throw new IllegalArgumentException("Имя пользователя и пользователь не могут быть null");
        }
        userMap.put(username, accountHolder);
    }

    @Override
    public AccountHolder find(String username) {
        return userMap.get(username);
    }

    @Override
    public void delete(String username) {
        if (username != null) {
            userMap.remove(username);
        }
    }

    @Override
    public Map<String, AccountHolder> findAll() {
        return new HashMap<>(userMap); // Возвращаем копию для безопасности
    }

    @Override
    public void setAllUsers(Map<String, AccountHolder> users) {
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
}