package com.business.entities;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
public class FinancialAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<FinancialEntry> financialEntries;
    private Map<String, Double> budgetsCategories;

    public FinancialAccount() {
        this.financialEntries = new ArrayList<>();
        this.budgetsCategories = new TreeMap<>(); // TreeMap для сортировки по названию
    }

    public void addTransaction(FinancialEntry financialEntry) {
        if (financialEntry == null) {
            throw new IllegalArgumentException("Транзакция не может быть null");
        }
        this.financialEntries.add(financialEntry);
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
}