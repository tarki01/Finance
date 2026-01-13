package com.business.services;

import com.business.exception.PasswordMismatchException;
import com.business.exception.UserAlreadyExistsException;
import com.business.exception.UserMissingException;
import com.business.entities.AccountHolder;
import com.business.ports.UserStoragePort;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AuthenticationService {
    private final UserStoragePort userStoragePort;
    private AccountHolder currentAccountHolder;

    public AuthenticationService(UserStoragePort userStoragePort) {
        this.userStoragePort = userStoragePort;
    }

    public AccountHolder handleLogin(String username, String password) {
        if (userStoragePort.containsUser(username)) {
            return login(username, password);
        } else {
            return registration(username, password);
        }
    }

    public void setUserMap(Map<String, AccountHolder> userMap) {
        if (userMap != null) {
            userStoragePort.setAllUsers(userMap);
        }
    }

    public Map<String, AccountHolder> getUserMap() {
        return userStoragePort.findAll();
    }

    public AccountHolder registration(String username, String password) {
        validateCredentials(username, password);

        if (userStoragePort.containsUser(username)) {
            throw new UserAlreadyExistsException(
                    "Пользователь с именем '" + username + "' уже существует");
        }

        AccountHolder newAccountHolder = new AccountHolder(username.trim(), password.trim());
        userStoragePort.save(username.trim(), newAccountHolder);
        currentAccountHolder = newAccountHolder;
        return currentAccountHolder;
    }

    public AccountHolder login(String username, String password) {
        validateCredentials(username, password);

        AccountHolder accountHolder = userStoragePort.find(username.trim());
        if (accountHolder == null) {
            throw new UserMissingException(
                    "Пользователь '" + username + "' не найден");
        }

        if (!password.trim().equals(accountHolder.getPassword())) {
            throw new PasswordMismatchException("Неверный пароль");
        }

        currentAccountHolder = accountHolder;
        return accountHolder;
    }

    public void unLogin() {
        currentAccountHolder = null;
    }

    public Boolean isLoggedIn() {
        return currentAccountHolder != null;
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
}