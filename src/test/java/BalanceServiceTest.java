import com.core.model.Transaction;
import com.core.model.User;
import com.core.model.Wallet;
import com.core.service.BalanceService;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class BalanceServiceTest {
    BalanceService balanceService = new BalanceService();
    User user = new User("test", "test");
    Wallet wallet = user.getWallet();
    @Test @DisplayName("Проверка начисления пользователю")
    public void addIncomeTest() {
        balanceService.addIncome(user, "test", 1d);
        Assert.assertNotNull(wallet.getTransactions().stream().anyMatch(transaction -> transaction.getIsIncome()));
    }
    @Test @DisplayName("Проверка вывода всей суммы начислений пользователя")
    public void getAllIncomeTest() {
        balanceService.addIncome(user, "test", 2d);
        balanceService.addIncome(user, "test", 1d);
        Assert.assertEquals(3d, balanceService.getAllIncome(user), 0);
    }
    @Test @DisplayName("Проверка начисления трат пользователю")
    public void addOutcomeTest() {
        balanceService.addOutcome(user, "test", 1d);
        Assert.assertNotNull(wallet.getTransactions().stream().anyMatch(transaction -> !transaction.getIsIncome()));
    }
    @Test @DisplayName("Проверка вывода всей суммы трат пользователя")
    public void getAllOutcomeTest() {
        balanceService.addOutcome(user, "test", 2d);
        Assert.assertEquals(2d, balanceService.getAllOutcome(user), 0);
    }
    @Test @DisplayName("Вывод начислений по категориям")
    public void getIncomeByCategoryTest() {
        balanceService.addIncome(user, "test", 3d);
        balanceService.addIncome(user, "test2", 6d);
        Map<String, Double> category = balanceService.getIncomeByCategory(user);

        Assert.assertEquals(3d, category.get("test"), 0);
        Assert.assertEquals(6d, category.get("test2"), 0);
    }
    @Test@DisplayName("Вывод трат по категориям")
    public void getOutcomeByCategoryTest(){
        balanceService.addOutcome(user, "test", 3d);
        balanceService.addOutcome(user, "test2", 6d);
        Map<String, Double> category = balanceService.getOutcomeByCategory(user);
        Assert.assertEquals(3d, category.get("test"), 0);
        Assert.assertEquals(6d, category.get("test2"), 0);
    }
    @Test@DisplayName("Тест данных по выводу категорий в определенное время")
    public void getIncomeByCategoryTest2(){
        balanceService.addIncome(user, "test", 3d);
        balanceService.addIncome(user, "test2", 6d);
        LocalDateTime datetime = LocalDateTime.parse("2002.02.10 10:10:10", DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
        LocalDateTime datetime2 = LocalDateTime.parse("2026.02.10 10:10:10", DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
        List<Transaction> category = balanceService.getTransactionByCategories(user, datetime, datetime2, new HashSet<>(List.of(Arrays.stream("test, test2".split(", ")).toArray())));
        Assert.assertEquals(3d, category.stream().filter(t -> t.getCategory().equals("test")).findAny().orElseGet(() -> null).getAmount(), 0);
        Assert.assertEquals(6d, category.stream().filter(t -> t.getCategory().equals("test2")).findAny().orElseGet(() -> null).getAmount() , 0);
    }

    @Test
    void budgetOverLimitPersent() {
        balanceService.setBudget(user, "test", 100d);
        balanceService.addOutcome(user, "test", 70d);
        Assert.assertEquals(false, balanceService.budgetOverLimitPersent(user, "test", 80d));
    }
    @Test
    void budgetIsZero() {
        balanceService.setBudget(user, "test", 100d);
        balanceService.addOutcome(user, "test", 101d);
        Assert.assertEquals(false, balanceService.budgetIsZero(user, "test"));
    }
    @Test
    @DisplayName("Проверка установки бюджета для категории")
    public void setBudgetTest() {
        balanceService.setBudget(user, "food", 500.0);
        Assert.assertEquals(500.0, user.getWallet().getBudget("food"), 0);
    }

    @Test
    @DisplayName("Получение бюджета категории когда он не установлен")
    public void getBudgetWhenNotSetTest() {
        Assert.assertEquals(0.0, balanceService.getBudget(user, "unknown"), 0);
    }

    @Test
    @DisplayName("Получение оставшегося бюджета категории")
    public void getBudgetCategoryTest() {
        balanceService.setBudget(user, "food", 1000.0);
        balanceService.addOutcome(user, "food", 300.0);
        Assert.assertEquals(700.0, balanceService.getBudgetCategory(user, "food"), 0);
    }

    @Test
    @DisplayName("Оставшийся бюджет при отсутствии трат")
    public void getBudgetCategoryNoSpendingTest() {
        balanceService.setBudget(user, "food", 500.0);
        Assert.assertEquals(500.0, balanceService.getBudgetCategory(user, "food"), 0);
    }

    @Test
    @DisplayName("Проверка превышения бюджета")
    public void budgetOverLimitTest() {
        balanceService.setBudget(user, "food", 100.0);
        balanceService.addOutcome(user, "food", 150.0);
        Assert.assertTrue(balanceService.budgetOverLimit(user, "food"));
    }

    @Test
    @DisplayName("Проверка нулевого остатка бюджета")
    public void budgetIsZeroTest() {
        balanceService.setBudget(user, "food", 100.0);
        balanceService.addOutcome(user, "food", 100.0);
        Assert.assertTrue(balanceService.budgetIsZero(user, "food"));
    }

    @Test
    @DisplayName("Проверка превышения бюджета по проценту")
    public void budgetOverLimitPercentTest() {
        balanceService.setBudget(user, "food", 1000.0);
        balanceService.addOutcome(user, "food", 800.0);
        Assert.assertTrue(balanceService.budgetOverLimitPersent(user, "food", 80.0));
    }

    @Test
    @DisplayName("Проверка когда расходы превышают доходы")
    public void outcomeOverIncomeAllTest() {
        balanceService.addIncome(user, "salary", 1000.0);
        balanceService.addOutcome(user, "rent", 1200.0);
        Assert.assertTrue(balanceService.outcomeOverIncomeAll(user));
    }

    @Test
    @DisplayName("Получение транзакций по категориям без фильтра по времени")
    public void getTransactionByCategoriesTest() {
        balanceService.addIncome(user, "food", 100.0);
        balanceService.addOutcome(user, "transport", 50.0);

        List<Transaction> transactions = balanceService.getTransactionByCategories(
                user,
                new HashSet<>(Arrays.asList("food", "transport"))
        );

        Assert.assertEquals(2, transactions.size());
    }

    @Test
    @DisplayName("Получение транзакций по категориям с фильтром по времени")
    public void getTransactionByCategoriesWithTimeFilterTest() {
        LocalDateTime baseTime = LocalDateTime.now();

        Transaction oldTransaction = new Transaction(100.0, "food", true);
        oldTransaction.setTimestamp(baseTime.minusDays(10));
        user.getWallet().addTransaction(oldTransaction);

        Transaction newTransaction = new Transaction(200.0, "food", true);
        newTransaction.setTimestamp(baseTime.minusDays(2));
        user.getWallet().addTransaction(newTransaction);

        List<Transaction> transactions = balanceService.getTransactionByCategories(
                user,
                baseTime.minusDays(5),
                baseTime.plusDays(1),
                new HashSet<>(List.of("food"))
        );

        Assert.assertEquals(1, transactions.size());
        Assert.assertEquals(200.0, transactions.get(0).getAmount(), 0);
    }

    @Test
    @DisplayName("Получение потраченной суммы для несуществующей категории")
    public void getSpentForUnknownCategoryTest() {
        Assert.assertEquals(0.0, balanceService.getSpent(user, "unknown"), 0);
    }

    @Test
    @DisplayName("Проверка отрицательного значения при добавлении дохода")
    public void addIncomeNegativeAmountTest() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            balanceService.addIncome(user, "food", -100.0);
        });
    }

    @Test
    @DisplayName("Получение транзакций по несуществующим категориям")
    public void getTransactionByNonExistentCategoriesTest() {
        List<Transaction> transactions = balanceService.getTransactionByCategories(
                user,
                new HashSet<>(List.of("unknown"))
        );
        Assert.assertTrue(transactions.isEmpty());
    }

}
