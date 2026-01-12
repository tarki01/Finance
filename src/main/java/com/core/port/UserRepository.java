package com.core.port;

import com.core.model.User;

import java.util.Map;

public interface UserRepository {
    void save(String username, User user);
    User find(String username);
    void delete(String username);
    Map<String, User> findAll();
    void setAllUsers(Map<String, User> users);
    boolean containsUser(String username);
}
