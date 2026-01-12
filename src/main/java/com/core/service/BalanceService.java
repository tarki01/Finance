package com.core.service;

import com.core.exception.CategoryNotFound;
import com.core.model.Transaction;
import com.core.model.User;
import com.core.model.Wallet;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BalanceService {

    public void addIncome(User user, String category, double amount) {
        validateTransactionInput(category, amount);
        Wallet userWallet = user.getWallet();
        Transaction transaction = new Transaction(amount, category, true);
        userWallet.addTransaction(transaction);
    }

    public void addOutcome(User user, String category, double amount) {
        validateTransactionInput(category, amount);
        Wallet userWallet = user.getWallet();
        Transaction transaction = new Transaction(amount, category, false);
        userWallet.addTransaction(transaction);
    }

    private void validateTransactionInput(String category, double amount) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория не может быть пустой");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
    }

    public double getAllIncome(User user) {
        Wallet userWallet = user.getWallet();
        return userWallet.getTransactions().stream()
                .filter(Transaction::getIsIncome)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getAllOutcome(User user) {
        Wallet userWallet = user.getWallet();
        return userWallet.getTransactions().stream()
                .filter(transaction -> !transaction.getIsIncome())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getCurrentBalance(User user) {
        return getAllIncome(user) - getAllOutcome(user);
    }

    public Map<String, Double> getIncomeByCategory(User user) {
        Wallet userWallet = user.getWallet();
        return userWallet.getTransactions().stream()
                .filter(Transaction::getIsIncome)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        TreeMap::new,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public Map<String, Double> getOutcomeByCategory(User user) {
        Wallet userWallet = user.getWallet();
        return userWallet.getTransactions().stream()
                .filter(t -> !t.getIsIncome())
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        TreeMap::new,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public Double getBudgetCategory(User user, String category) {
        return getBudget(user, category) - getSpent(user, category);
    }

    public double getBudget(User user, String category) {
        Double budget = user.getWallet().getBudget(category);
        return budget != null ? budget : 0.0;
    }

    public double getSpent(User user, String category) {
        return getOutcomeByCategory(user).getOrDefault(category, 0.0);
    }

    // Оповещать пользователя, если превышен лимит бюджета по категории
    public boolean budgetOverLimit(User user, String category) {
        return getBudgetCategory(user, category) < 0;
    }

    public boolean budgetIsZero(User user, String category) {
        return Math.abs(getBudgetCategory(user, category)) < 0.01;
    }

    public boolean budgetOverLimitPercent(User user, String category, double percent) {
        double budget = getBudget(user, category);
        double spent = getSpent(user, category);
        if (budget == 0) return false;
        return (spent >= (budget * percent) / 100);
    }

    // Оповещать пользователя, если расходы превысили доходы.
    public boolean outcomeOverIncomeAll(User user) {
        return getAllOutcome(user) > getAllIncome(user);
    }

    public void setBudget(User user, String category, double amount) {
        validateBudgetInput(category, amount);
        Wallet userWallet = user.getWallet();
        userWallet.setBudget(category, amount);
    }

    private void validateBudgetInput(String category, double amount) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория бюджета не может быть пустой");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Бюджет не может быть отрицательным");
        }
    }

    public void removeBudget(User user, String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория не может быть пустой");
        }
        Wallet userWallet = user.getWallet();
        if (!userWallet.getBudgetsCategories().containsKey(category)) {
            throw new CategoryNotFound("Бюджет для категории '" + category + "' не найден");
        }
        userWallet.getBudgetsCategories().remove(category);
    }

    // Гибкий выбор категорий или периода, корректные уведомления при отсутствии данных
    public List<Transaction> getTransactionByCategories(User user, Set<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }

        Wallet userWallet = user.getWallet();
        return userWallet.getTransactions().stream()
                .filter(t -> categories.contains(t.getCategory()))
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionByCategories(User user, LocalDateTime timeFrom,
                                                        LocalDateTime timeTo, Set<String> categories) {
        List<Transaction> list = getTransactionByCategories(user, categories);
        if (list.isEmpty()) {
            return list;
        }

        return list.stream()
                .filter(t -> !t.getTimestamp().isBefore(timeFrom) && !t.getTimestamp().isAfter(timeTo))
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories(User user) {
        Wallet userWallet = user.getWallet();
        return userWallet.getTransactions().stream()
                .map(Transaction::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getBudgetCategories(User user) {
        return new ArrayList<>(user.getWallet().getBudgetsCategories().keySet())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }
}