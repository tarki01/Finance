package com.core.service;

import com.core.exception.PasswordNotFoundException;
import com.core.exception.UserAlreadyCreatedException;
import com.core.exception.UserNotFoundException;
import com.core.model.User;
import com.core.port.UserRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class LoginService {
    private final UserRepository userRepository;
    private User currentUser;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleLogin(String username, String password) {
        if (userRepository.containsUser(username)) {
            return login(username, password);
        } else {
            return registration(username, password);
        }
    }

    public void setUserMap(Map<String, User> userMap) {
        if (userMap != null) {
            userRepository.setAllUsers(userMap);
        }
    }

    public Map<String, User> getUserMap() {
        return userRepository.findAll();
    }

    public User registration(String username, String password) {
        validateCredentials(username, password);

        if (userRepository.containsUser(username)) {
            throw new UserAlreadyCreatedException(
                    "Пользователь с именем '" + username + "' уже существует");
        }

        User newUser = new User(username.trim(), password.trim());
        userRepository.save(username.trim(), newUser);
        currentUser = newUser;
        return currentUser;
    }

    public User login(String username, String password) {
        validateCredentials(username, password);

        User user = userRepository.find(username.trim());
        if (user == null) {
            throw new UserNotFoundException(
                    "Пользователь '" + username + "' не найден");
        }

        if (!password.trim().equals(user.getPassword())) {
            throw new PasswordNotFoundException("Неверный пароль");
        }

        currentUser = user;
        return user;
    }

    public void unLogin() {
        currentUser = null;
    }

    public Boolean isLoggedIn() {
        return currentUser != null;
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        if (username.trim().length() < 3) {
            throw new IllegalArgumentException(
                    "Имя пользователя должно содержать не менее 3 символов");
        }

        if (password.trim().length() < 4) {
            throw new IllegalArgumentException(
                    "Пароль должен содержать не менее 4 символов");
        }
    }

    public boolean userExists(String username) {
        return userRepository.containsUser(username);
    }
}