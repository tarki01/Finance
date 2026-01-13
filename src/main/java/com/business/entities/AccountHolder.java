package com.business.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class AccountHolder implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private FinancialAccount financialAccount;

    public AccountHolder(String username, String password) {
        setUsername(username);
        setPassword(password);
        this.financialAccount = new FinancialAccount();
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        this.username = username.trim();
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        this.password = password.trim();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AccountHolder accountHolder = (AccountHolder) obj;
        return username.equals(accountHolder.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Пользователь: %s (транзакций: %d, бюджетов: %d)",
                username,
                financialAccount.getFinancialEntries().size(),
                financialAccount.getBudgetsCategories().size());
    }
}