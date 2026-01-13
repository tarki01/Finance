package com.business.services;

import com.business.exception.CategoryMissingException;
import com.business.entities.FinancialEntry;
import com.business.entities.AccountHolder;
import com.business.entities.FinancialAccount;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FinancialOperationsService {

    public void addIncome(AccountHolder accountHolder, String category, double amount) {
        validateTransactionInput(category, amount);
        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        FinancialEntry financialEntry = new FinancialEntry(amount, category, true);
        userFinancialAccount.addTransaction(financialEntry);
    }

    public void addOutcome(AccountHolder accountHolder, String category, double amount) {
        validateTransactionInput(category, amount);
        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        FinancialEntry financialEntry = new FinancialEntry(amount, category, false);
        userFinancialAccount.addTransaction(financialEntry);
    }

    private void validateTransactionInput(String category, double amount) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория не может быть пустой");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
    }

    public double getAllIncome(AccountHolder accountHolder) {
        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        return userFinancialAccount.getFinancialEntries().stream()
                .filter(FinancialEntry::getIsIncome)
                .mapToDouble(FinancialEntry::getAmount)
                .sum();
    }

    public double getAllOutcome(AccountHolder accountHolder) {
        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        return userFinancialAccount.getFinancialEntries().stream()
                .filter(transaction -> !transaction.getIsIncome())
                .mapToDouble(FinancialEntry::getAmount)
                .sum();
    }

    public double getCurrentBalance(AccountHolder accountHolder) {
        return getAllIncome(accountHolder) - getAllOutcome(accountHolder);
    }

    public Map<String, Double> getIncomeByCategory(AccountHolder accountHolder) {
        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        return userFinancialAccount.getFinancialEntries().stream()
                .filter(FinancialEntry::getIsIncome)
                .collect(Collectors.groupingBy(
                        FinancialEntry::getCategory,
                        TreeMap::new,
                        Collectors.summingDouble(FinancialEntry::getAmount)
                ));
    }

    public Map<String, Double> getOutcomeByCategory(AccountHolder accountHolder) {
        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        return userFinancialAccount.getFinancialEntries().stream()
                .filter(t -> !t.getIsIncome())
                .collect(Collectors.groupingBy(
                        FinancialEntry::getCategory,
                        TreeMap::new,
                        Collectors.summingDouble(FinancialEntry::getAmount)
                ));
    }

    public Double getBudgetCategory(AccountHolder accountHolder, String category) {
        return getBudget(accountHolder, category) - getSpent(accountHolder, category);
    }

    public double getBudget(AccountHolder accountHolder, String category) {
        Double budget = accountHolder.getFinancialAccount().getBudget(category);
        return budget != null ? budget : 0.0;
    }

    public double getSpent(AccountHolder accountHolder, String category) {
        return getOutcomeByCategory(accountHolder).getOrDefault(category, 0.0);
    }

    // Оповещать пользователя, если превышен лимит бюджета по категории
    public boolean budgetOverLimit(AccountHolder accountHolder, String category) {
        return getBudgetCategory(accountHolder, category) < 0;
    }

    public boolean budgetIsZero(AccountHolder accountHolder, String category) {
        return Math.abs(getBudgetCategory(accountHolder, category)) < 0.01;
    }

    public boolean budgetOverLimitPercent(AccountHolder accountHolder, String category, double percent) {
        double budget = getBudget(accountHolder, category);
        double spent = getSpent(accountHolder, category);
        if (budget == 0) return false;
        return (spent >= (budget * percent) / 100);
    }

    // Оповещать пользователя, если расходы превысили доходы.
    public boolean outcomeOverIncomeAll(AccountHolder accountHolder) {
        return getAllOutcome(accountHolder) > getAllIncome(accountHolder);
    }

    public void setBudget(AccountHolder accountHolder, String category, double amount) {
        validateBudgetInput(category, amount);
        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        userFinancialAccount.setBudget(category, amount);
    }

    private void validateBudgetInput(String category, double amount) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория бюджета не может быть пустой");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Бюджет не может быть отрицательным");
        }
    }

    public void removeBudget(AccountHolder accountHolder, String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория не может быть пустой");
        }
        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        if (!userFinancialAccount.getBudgetsCategories().containsKey(category)) {
            throw new CategoryMissingException("Бюджет для категории '" + category + "' не найден");
        }
        userFinancialAccount.getBudgetsCategories().remove(category);
    }

    // Гибкий выбор категорий или периода, корректные уведомления при отсутствии данных
    public List<FinancialEntry> getTransactionByCategories(AccountHolder accountHolder, Set<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }

        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        return userFinancialAccount.getFinancialEntries().stream()
                .filter(t -> categories.contains(t.getCategory()))
                .collect(Collectors.toList());
    }

    public List<FinancialEntry> getTransactionByCategories(AccountHolder accountHolder, LocalDateTime timeFrom,
                                                           LocalDateTime timeTo, Set<String> categories) {
        List<FinancialEntry> list = getTransactionByCategories(accountHolder, categories);
        if (list.isEmpty()) {
            return list;
        }

        return list.stream()
                .filter(t -> !t.getTimestamp().isBefore(timeFrom) && !t.getTimestamp().isAfter(timeTo))
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories(AccountHolder accountHolder) {
        FinancialAccount userFinancialAccount = accountHolder.getFinancialAccount();
        return userFinancialAccount.getFinancialEntries().stream()
                .map(FinancialEntry::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getBudgetCategories(AccountHolder accountHolder) {
        return new ArrayList<>(accountHolder.getFinancialAccount().getBudgetsCategories().keySet())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }
}