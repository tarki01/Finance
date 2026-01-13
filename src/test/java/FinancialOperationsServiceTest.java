import com.business.entities.FinancialEntry;
import com.business.entities.AccountHolder;
import com.business.exception.CategoryMissingException;
import com.business.services.FinancialOperationsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FinancialOperationsServiceTest {
    private FinancialOperationsService financialOperationsService;
    private AccountHolder accountHolder;

    @BeforeEach
    public void setUp() {
        financialOperationsService = new FinancialOperationsService();
        accountHolder = new AccountHolder("test", "test");
    }

    @Test
    @DisplayName("Проверка начисления пользователю")
    public void addIncomeTest() {
        financialOperationsService.addIncome(accountHolder, "test", 1d);
        assertEquals(1, accountHolder.getFinancialAccount().getFinancialEntries().size());
        assertTrue(accountHolder.getFinancialAccount().getFinancialEntries().get(0).getIsIncome());
    }

    @Test
    @DisplayName("Проверка вывода всей суммы начислений пользователя")
    public void getAllIncomeTest() {
        financialOperationsService.addIncome(accountHolder, "test", 2d);
        financialOperationsService.addIncome(accountHolder, "test", 1d);
        assertEquals(3d, financialOperationsService.getAllIncome(accountHolder), 0.001);
    }

    @Test
    @DisplayName("Проверка начисления трат пользователю")
    public void addOutcomeTest() {
        financialOperationsService.addOutcome(accountHolder, "test", 1d);
        assertEquals(1, accountHolder.getFinancialAccount().getFinancialEntries().size());
        assertFalse(accountHolder.getFinancialAccount().getFinancialEntries().get(0).getIsIncome());
    }

    @Test
    @DisplayName("Проверка вывода всей суммы трат пользователя")
    public void getAllOutcomeTest() {
        financialOperationsService.addOutcome(accountHolder, "test", 2d);
        assertEquals(2d, financialOperationsService.getAllOutcome(accountHolder), 0.001);
    }

    @Test
    @DisplayName("Вывод начислений по категориям")
    public void getIncomeByCategoryTest() {
        financialOperationsService.addIncome(accountHolder, "test", 3d);
        financialOperationsService.addIncome(accountHolder, "test2", 6d);
        Map<String, Double> category = financialOperationsService.getIncomeByCategory(accountHolder);

        assertEquals(3d, category.get("test"), 0.001);
        assertEquals(6d, category.get("test2"), 0.001);
    }

    @Test
    @DisplayName("Вывод трат по категориям")
    public void getOutcomeByCategoryTest() {
        financialOperationsService.addOutcome(accountHolder, "test", 3d);
        financialOperationsService.addOutcome(accountHolder, "test2", 6d);
        Map<String, Double> category = financialOperationsService.getOutcomeByCategory(accountHolder);
        assertEquals(3d, category.get("test"), 0.001);
        assertEquals(6d, category.get("test2"), 0.001);
    }

    @Test
    @DisplayName("Тест данных по выводу категорий в определенное время")
    public void getIncomeByCategoryWithTimeTest() {
        financialOperationsService.addIncome(accountHolder, "test", 3d);
        financialOperationsService.addIncome(accountHolder, "test2", 6d);
        LocalDateTime datetime = LocalDateTime.parse("2002-02-10T10:10:10");
        LocalDateTime datetime2 = LocalDateTime.parse("2026-02-10T10:10:10");

        Set<String> categories = new HashSet<>(Arrays.asList("test", "test2"));
        List<FinancialEntry> category = financialOperationsService.getTransactionByCategories(
                accountHolder, datetime, datetime2, categories);

        assertEquals(2, category.size());
        assertTrue(category.stream().anyMatch(t -> t.getCategory().equals("test") && t.getAmount() == 3d));
        assertTrue(category.stream().anyMatch(t -> t.getCategory().equals("test2") && t.getAmount() == 6d));
    }

    @Test
    @DisplayName("Проверка превышения бюджета по проценту")
    public void budgetOverLimitPercentTest() {
        financialOperationsService.setBudget(accountHolder, "test", 100d);
        financialOperationsService.addOutcome(accountHolder, "test", 81d);
        assertTrue(financialOperationsService.budgetOverLimitPercent(accountHolder, "test", 80d));
    }

    @Test
    @DisplayName("Проверка нулевого бюджета")
    public void budgetIsZeroTest() {
        financialOperationsService.setBudget(accountHolder, "test", 100d);
        financialOperationsService.addOutcome(accountHolder, "test", 100d);
        assertTrue(financialOperationsService.budgetIsZero(accountHolder, "test"));
    }

    @Test
    @DisplayName("Проверка установки бюджета для категории")
    public void setBudgetTest() {
        financialOperationsService.setBudget(accountHolder, "food", 500.0);
        assertEquals(500.0, accountHolder.getFinancialAccount().getBudget("food"), 0.001);
    }

    @Test
    @DisplayName("Получение бюджета категории когда он не установлен")
    public void getBudgetWhenNotSetTest() {
        assertEquals(0.0, financialOperationsService.getBudget(accountHolder, "unknown"), 0.001);
    }

    @Test
    @DisplayName("Получение оставшегося бюджета категории")
    public void getBudgetCategoryTest() {
        financialOperationsService.setBudget(accountHolder, "food", 1000.0);
        financialOperationsService.addOutcome(accountHolder, "food", 300.0);
        assertEquals(700.0, financialOperationsService.getBudgetCategory(accountHolder, "food"), 0.001);
    }

    @Test
    @DisplayName("Оставшийся бюджет при отсутствии трат")
    public void getBudgetCategoryNoSpendingTest() {
        financialOperationsService.setBudget(accountHolder, "food", 500.0);
        assertEquals(500.0, financialOperationsService.getBudgetCategory(accountHolder, "food"), 0.001);
    }

    @Test
    @DisplayName("Проверка превышения бюджета")
    public void budgetOverLimitTest() {
        financialOperationsService.setBudget(accountHolder, "food", 100.0);
        financialOperationsService.addOutcome(accountHolder, "food", 150.0);
        assertTrue(financialOperationsService.budgetOverLimit(accountHolder, "food"));
    }

    @Test
    @DisplayName("Проверка когда расходы превышают доходы")
    public void outcomeOverIncomeAllTest() {
        financialOperationsService.addIncome(accountHolder, "salary", 1000.0);
        financialOperationsService.addOutcome(accountHolder, "rent", 1200.0);
        assertTrue(financialOperationsService.outcomeOverIncomeAll(accountHolder));
    }

    @Test
    @DisplayName("Получение транзакций по категориям без фильтра по времени")
    public void getTransactionByCategoriesTest() {
        financialOperationsService.addIncome(accountHolder, "food", 100.0);
        financialOperationsService.addOutcome(accountHolder, "transport", 50.0);

        List<FinancialEntry> financialEntries = financialOperationsService.getTransactionByCategories(
                accountHolder,
                new HashSet<>(Arrays.asList("food", "transport"))
        );

        assertEquals(2, financialEntries.size());
    }

    @Test
    @DisplayName("Получение транзакций по категориям с фильтром по времени")
    public void getTransactionByCategoriesWithTimeFilterTest() {
        LocalDateTime baseTime = LocalDateTime.now();

        // Создаем транзакцию 10 дней назад
        FinancialEntry oldFinancialEntry = new FinancialEntry(100.0, "food", true);
        oldFinancialEntry.setTimestamp(baseTime.minusDays(10));
        accountHolder.getFinancialAccount().addTransaction(oldFinancialEntry);

        // Создаем транзакцию 2 дня назад
        FinancialEntry newFinancialEntry = new FinancialEntry(200.0, "food", true);
        newFinancialEntry.setTimestamp(baseTime.minusDays(2));
        accountHolder.getFinancialAccount().addTransaction(newFinancialEntry);

        List<FinancialEntry> financialEntries = financialOperationsService.getTransactionByCategories(
                accountHolder,
                baseTime.minusDays(5),
                baseTime.plusDays(1),
                new HashSet<>(List.of("food"))
        );

        assertEquals(1, financialEntries.size());
        assertEquals(200.0, financialEntries.get(0).getAmount(), 0.001);
    }

    @Test
    @DisplayName("Получение потраченной суммы для несуществующей категории")
    public void getSpentForUnknownCategoryTest() {
        assertEquals(0.0, financialOperationsService.getSpent(accountHolder, "unknown"), 0.001);
    }

    @Test
    @DisplayName("Проверка отрицательного значения при добавлении дохода")
    public void addIncomeNegativeAmountTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            financialOperationsService.addIncome(accountHolder, "food", -100.0);
        });
    }

    @Test
    @DisplayName("Получение транзакций по несуществующим категориям")
    public void getTransactionByNonExistentCategoriesTest() {
        List<FinancialEntry> financialEntries = financialOperationsService.getTransactionByCategories(
                accountHolder,
                new HashSet<>(List.of("unknown"))
        );
        assertTrue(financialEntries.isEmpty());
    }

    @Test
    @DisplayName("Удаление бюджета существующей категории")
    public void removeBudgetExistingCategoryTest() {
        financialOperationsService.setBudget(accountHolder, "food", 500.0);
        financialOperationsService.removeBudget(accountHolder, "food");
        assertNull(accountHolder.getFinancialAccount().getBudget("food"));
    }

    @Test
    @DisplayName("Удаление бюджета несуществующей категории должно бросать исключение")
    public void removeBudgetNonExistentCategoryTest() {
        assertThrows(CategoryMissingException.class, () -> {
            financialOperationsService.removeBudget(accountHolder, "unknown");
        });
    }

    @Test
    @DisplayName("Получение всех категорий")
    public void getAllCategoriesTest() {
        financialOperationsService.addIncome(accountHolder, "food", 100.0);
        financialOperationsService.addOutcome(accountHolder, "transport", 50.0);
        financialOperationsService.addIncome(accountHolder, "food", 200.0); // Дублирующая категория

        List<String> categories = financialOperationsService.getAllCategories(accountHolder);
        assertEquals(2, categories.size());
        assertTrue(categories.contains("food"));
        assertTrue(categories.contains("transport"));
    }

    @Test
    @DisplayName("Получение категорий с бюджетами")
    public void getBudgetCategoriesTest() {
        financialOperationsService.setBudget(accountHolder, "food", 500.0);
        financialOperationsService.setBudget(accountHolder, "transport", 300.0);

        List<String> categories = financialOperationsService.getBudgetCategories(accountHolder);
        assertEquals(2, categories.size());
        assertTrue(categories.contains("food"));
        assertTrue(categories.contains("transport"));
    }

    @Test
    @DisplayName("Текущий баланс пользователя")
    public void getCurrentBalanceTest() {
        financialOperationsService.addIncome(accountHolder, "salary", 1000.0);
        financialOperationsService.addOutcome(accountHolder, "rent", 400.0);
        financialOperationsService.addOutcome(accountHolder, "food", 200.0);

        assertEquals(400.0, financialOperationsService.getCurrentBalance(accountHolder), 0.001);
    }

    @Test
    @DisplayName("Добавление транзакции с пустой категорией должно бросать исключение")
    public void addTransactionWithEmptyCategoryTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            financialOperationsService.addIncome(accountHolder, "", 100.0);
        });
    }

    @Test
    @DisplayName("Установка бюджета с отрицательной суммой должно бросать исключение")
    public void setBudgetWithNegativeAmountTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            financialOperationsService.setBudget(accountHolder, "food", -100.0);
        });
    }

    @Test
    @DisplayName("Установка бюджета с пустой категорией должно бросать исключение")
    public void setBudgetWithEmptyCategoryTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            financialOperationsService.setBudget(accountHolder, "", 100.0);
        });
    }
}