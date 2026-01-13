package com.business.ports;

import com.business.entities.AccountHolder;

import java.util.Map;

public interface UserStoragePort {
    void save(String username, AccountHolder accountHolder);
    AccountHolder find(String username);
    void delete(String username);
    Map<String, AccountHolder> findAll();
    void setAllUsers(Map<String, AccountHolder> users);
    boolean containsUser(String username);
}
