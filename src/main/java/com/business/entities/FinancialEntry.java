package com.business.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class FinancialEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private Double amount;
    private String category;
    private Boolean isIncome;
    private LocalDateTime timestamp;

    public FinancialEntry(Double amount, String category, Boolean isIncome) {
        this.amount = amount;
        this.category = category;
        this.isIncome = isIncome;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FinancialEntry t = (FinancialEntry) o;
        return t.getCategory().equals(this.category)
                && t.getAmount().equals(this.amount)
                && t.getTimestamp().equals(this.timestamp)
                && t.getIsIncome().equals(this.isIncome);
    }

    @Override
    public int hashCode() {
        int result = amount != null ? amount.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (isIncome != null ? isIncome.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String type = isIncome ? "Доход" : "Расход";
        String sign = isIncome ? "+" : "-";
        return String.format("%s: %s%.2f (%s) [%s]",
                type, sign, amount, category,
                timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    }
}