package com.core.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
public class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Transaction> transactions;
    private Map<String, Double> budgetsCategories;

    public Wallet() {
        this.transactions = new ArrayList<>();
        this.budgetsCategories = new TreeMap<>(); // TreeMap для сортировки по названию
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Транзакция не может быть null");
        }
        this.transactions.add(transaction);
    }

    public Transaction getTransaction(int index) {
        if (index < 0 || index >= transactions.size()) {
            throw new IndexOutOfBoundsException("Неверный индекс транзакции: " + index);
        }
        return this.transactions.get(index);
    }

    public void setBudget(String category, double amount) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория не может быть пустой");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Бюджет не может быть отрицательным");
        }
        this.budgetsCategories.put(category.trim(), amount);
    }

    public Double getBudget(String category) {
        if (category == null) {
            return null;
        }
        return this.budgetsCategories.get(category);
    }

    public boolean hasBudget(String category) {
        return category != null && budgetsCategories.containsKey(category);
    }

    public void removeBudget(String category) {
        if (category != null) {
            budgetsCategories.remove(category);
        }
    }

    public List<String> getTransactionCategories() {
        List<String> categories = new ArrayList<>();
        for (Transaction t : transactions) {
            if (!categories.contains(t.getCategory())) {
                categories.add(t.getCategory());
            }
        }
        categories.sort(String::compareTo);
        return categories;
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(Transaction::getIsIncome)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalOutcome() {
        return transactions.stream()
                .filter(t -> !t.getIsIncome())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getBalance() {
        return getTotalIncome() - getTotalOutcome();
    }
}