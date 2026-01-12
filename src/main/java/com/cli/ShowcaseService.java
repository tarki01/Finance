package com.cli;

import com.cli.enums.Examples;
import com.cli.enums.Help;
import com.core.model.Transaction;
import com.core.model.User;
import com.core.service.BalanceService;
import com.core.service.LoginService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for displaying user interface elements and statistics.
 */
public class ShowcaseService {
    /**
     * Login service instance.
     */
    private LoginService loginService;

    /**
     * Constant for percent limit of the budget.
     */
    private static final int LIMIT_PERCENTAGE = 80;

    /**
     * Balance service instance.
     */
    private BalanceService balanceService;

    /**
     * Format string for statistics.
     */
    private static final String REGEX_STATS = "%20s %n";

    /**
     * Date formatter for display.
     */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    /**
     * Constructor for ShowcaseService.
     *
     * @param loginServiceParam the login service
     * @param balanceServiceParam the balance service
     */
    public ShowcaseService(final LoginService loginServiceParam,
                           final BalanceService balanceServiceParam) {
        this.loginService = loginServiceParam;
        this.balanceService = balanceServiceParam;
    }

    /**
     * Displays the login menu.
     */
    public void showLoginMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("      ВХОД / РЕГИСТРАЦИЯ");
        System.out.println("=".repeat(40));
        System.out.println("1. Регистрация нового пользователя");
        System.out.println("2. Войти в существующий аккаунт");
        System.out.println("3. Выйти из приложения");
        System.out.println("4. Загрузить данные пользователя из файла");
        System.out.println("5. Показать справку (help)");
        System.out.println("=".repeat(40));
        System.out.print("Выберите действие (1-5): ");
    }

    /**
     * Displays the main menu.
     */
    public void showMainMenu() {
        User currentUser = loginService.getCurrentUser();
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        ГЛАВНОЕ МЕНЮ");
        System.out.println("=".repeat(40));
        System.out.println("Текущий пользователь: " +
                (currentUser != null ? currentUser.getUsername() : "не авторизован"));
        if (currentUser != null) {
            System.out.printf("Текущий баланс: %.2f%n",
                    balanceService.getCurrentBalance(currentUser));
        }
        System.out.println("=".repeat(40));
        System.out.println("1. Управление транзакциями");
        System.out.println("2. Управление бюджетом");
        System.out.println("3. Просмотр статистики");
        System.out.println("4. Перевести деньги другому пользователю");
        System.out.println("5. Выйти из аккаунта");
        System.out.println("6. Управление данными пользователя");
        System.out.println("7. Справка и примеры использования");
        System.out.println("8. Показать все транзакции");
        System.out.println("=".repeat(40));
        System.out.print("Выберите действие (1-8): ");
    }

    /**
     * Displays JSON operations menu.
     */
    public void showJsons() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("   УПРАВЛЕНИЕ ДАННЫМИ ПОЛЬЗОВАТЕЛЯ");
        System.out.println("=".repeat(50));
        System.out.println("1. Загрузить данные пользователя из файла");
        System.out.println("2. Сохранить данные пользователя в файл");
        System.out.println("3. Удалить текущего пользователя");
        System.out.println("4. Вернуться в главное меню");
        System.out.println("=".repeat(50));
        System.out.print("Выберите действие (1-4): ");
    }

    /**
     * Displays transaction operations menu.
     */
    public void showTransactionMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("       УПРАВЛЕНИЕ ТРАНЗАКЦИЯМИ");
        System.out.println("=".repeat(40));
        System.out.println("1. Добавить доход");
        System.out.println("2. Добавить расход");
        System.out.println("3. Показать все транзакции");
        System.out.println("4. Изменить существующую транзакцию");
        System.out.println("5. Удалить транзакцию");
        System.out.println("6. Вернуться назад");
        System.out.println("=".repeat(40));
        System.out.print("Выберите действие (1-6): ");
    }

    /**
     * Displays transaction editing options.
     */
    public void showChangeTransaction() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("   РЕДАКТИРОВАНИЕ ТРАНЗАКЦИИ");
        System.out.println("=".repeat(40));
        System.out.println("1. Изменить категорию");
        System.out.println("2. Изменить сумму");
        System.out.println("3. Изменить тип (доход / расход)");
        System.out.println("4. Отмена");
        System.out.println("=".repeat(40));
        System.out.print("Выберите действие (1-4): ");
    }

    /**
     * Displays all transactions for the current user with pagination.
     */
    public void showAllTransactions(boolean showActions) {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("Ошибка: пользователь не авторизован!");
            return;
        }

        List<Transaction> transactions = user.getWallet().getTransactions();
        if (transactions.isEmpty()) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("   СПИСОК ТРАНЗАКЦИЙ");
            System.out.println("=".repeat(40));
            System.out.println("Нет транзакций.");
            System.out.println("=".repeat(40));
            return;
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("                              СПИСОК ТРАНЗАКЦИЙ");
        System.out.println("=".repeat(80));
        System.out.printf("%-4s %-15s %-12s %-25s %-10s%n",
                "№", "Тип", "Сумма", "Категория", "Дата и время");
        System.out.println("-".repeat(80));

        int index = 1;
        for (Transaction t : transactions) {
            String type = t.getIsIncome() ? "Доход" : "Расход";
            String sign = t.getIsIncome() ? "+" : "-";
            String formattedDate = t.getTimestamp().format(DATE_FORMATTER);
            System.out.printf("%-4d %-15s %-12s %-25s %-10s%n",
                    index++,
                    type,
                    String.format("%s%.2f", sign, t.getAmount()),
                    t.getCategory(),
                    formattedDate);
        }
        System.out.println("=".repeat(80));

        if (showActions) {
            System.out.println("\nДальнейшие действия:");
            System.out.println("1. Удалить транзакцию");
            System.out.println("2. Изменить транзакцию");
            System.out.println("3. Вернуться в меню");
            System.out.print("Ваш выбор (1-3): ");
        }
    }

    /**
     * Displays all statistics for the current user.
     */
    public void showAllStatistic() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("Ошибка: пользователь не авторизован!");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("                  СТАТИСТИКА");
        System.out.println("=".repeat(60));
        System.out.println("\n--- Общие данные ---");
        System.out.printf("Общая сумма доходов: %.2f%n", balanceService.getAllIncome(user));
        System.out.printf("Общая сумма расходов: %.2f%n", balanceService.getAllOutcome(user));
        System.out.printf("Текущий баланс: %.2f%n", balanceService.getCurrentBalance(user));

        if (balanceService.outcomeOverIncomeAll(user)) {
            System.out.println("\n⚠️  ВНИМАНИЕ: Расходы превышают доходы!");
        }

        System.out.println("\n--- Бюджеты ---");
        printBudgets(user.getWallet().getBudgetsCategories());

        System.out.println("\n--- Доходы по категориям ---");
        printIncomes(balanceService.getIncomeByCategory(user));

        System.out.println("\n--- Расходы по категориям ---");
        printOutcomes(balanceService.getOutcomeByCategory(user));
        System.out.println("=".repeat(60));
    }

    /**
     * Displays statistics menu.
     */
    public void showStatistic() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        МЕНЮ СТАТИСТИКИ");
        System.out.println("=".repeat(40));
        System.out.println("1. Показать полную статистику");
        System.out.println("2. Показать статистику по времени и категориям");
        System.out.println("3. Показать доступные категории");
        System.out.println("4. Вернуться в меню");
        System.out.println("=".repeat(40));
        System.out.print("Выберите вариант (1-4): ");
    }

    /**
     * Displays statistics by category and time period.
     *
     * @param firstTime start time
     * @param secondTime end time
     * @param categories array of categories
     */
    public void showStatisticByCategory(final LocalDateTime firstTime,
                                        final LocalDateTime secondTime,
                                        final String[] categories) {
        try {
            User user = loginService.getCurrentUser();
            if (user == null) {
                System.out.println("Ошибка: пользователь не авторизован!");
                return;
            }

            Set<String> categoriesSet = new TreeSet<>(Arrays.asList(categories));
            if (categoriesSet.isEmpty() ||
                    (categoriesSet.size() == 1 && categoriesSet.iterator().next().isEmpty())) {
                categoriesSet.addAll(user.getWallet().getBudgetsCategories().keySet());
            }

            if (categoriesSet.isEmpty()) {
                System.out.println("Категории не указаны и бюджеты не установлены.");
                System.out.println("Доступные категории: " + balanceService.getAllCategories(user));
                return;
            }

            Map<String, Double> budgets = user.getWallet()
                    .getBudgetsCategories().entrySet().stream()
                    .filter(e -> categoriesSet.contains(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            System.out.println("\n" + "=".repeat(60));
            System.out.println("    СТАТИСТИКА ПО КАТЕГОРИЯМ И ВРЕМЕНИ");
            System.out.println("=".repeat(60));
            System.out.println("Выбранный период: " +
                    firstTime.format(DATE_FORMATTER) + " — " +
                    secondTime.format(DATE_FORMATTER));
            System.out.println("Выбранные категории: " + categoriesSet);
            System.out.println();

            if (!budgets.isEmpty()) {
                printBudgets(budgets);
            }

            Set<String> filteredCategories = categoriesSet.stream()
                    .filter(cat -> !cat.isEmpty())
                    .collect(Collectors.toSet());

            List<Transaction> list = balanceService.getTransactionByCategories(
                    user, firstTime, secondTime, filteredCategories);

            if (list.isEmpty()) {
                System.out.println("\nНет транзакций по выбранным категориям в указанный период.");
            } else {
                Map<String, Double> mapOfIncomes = list.stream()
                        .filter(Transaction::getIsIncome)
                        .collect(Collectors.toMap(
                                Transaction::getCategory,
                                Transaction::getAmount,
                                Double::sum,
                                TreeMap::new
                        ));
                if (!mapOfIncomes.isEmpty()) {
                    printIncomes(mapOfIncomes);
                }

                Map<String, Double> mapOfOutcomes = list.stream()
                        .filter(t -> !t.getIsIncome())
                        .collect(Collectors.toMap(
                                Transaction::getCategory,
                                Transaction::getAmount,
                                Double::sum,
                                TreeMap::new
                        ));
                if (!mapOfOutcomes.isEmpty()) {
                    printOutcomes(mapOfOutcomes);
                }

                System.out.printf("%nВсего найдено транзакций: %d%n", list.size());
            }

        } catch (Exception e) {
            System.out.println("Ошибка при получении статистики: " + e.getMessage());
        }
    }

    /**
     * Prints income statistics.
     *
     * @param getIncome map of categories to income amounts
     */
    public void printIncomes(final Map<String, Double> getIncome) {
        if (getIncome != null && !getIncome.isEmpty()) {
            System.out.printf("%-25s %-15s%n", "Категория", "Сумма доходов");
            System.out.println("-".repeat(40));
            getIncome.forEach((k, v) ->
                    System.out.printf("%-25s %-15.2f%n", k, v));
        } else {
            System.out.println("Нет данных о доходах.");
        }
    }

    /**
     * Prints budget information.
     *
     * @param budgets map of categories to budget amounts
     */
    public void printBudgets(final Map<String, Double> budgets) {
        if (budgets != null && !budgets.isEmpty()) {
            System.out.printf("%-25s %-15s%n", "Категория", "Бюджет");
            System.out.println("-".repeat(40));
            budgets.forEach((k, v) ->
                    System.out.printf("%-25s %-15.2f%n", k, v));
        } else {
            System.out.println("Бюджеты не установлены.");
        }
    }

    /**
     * Prints outcome statistics with budget information.
     *
     * @param getOutcome map of categories to outcome amounts
     */
    public void printOutcomes(final Map<String, Double> getOutcome) {
        User user = loginService.getCurrentUser();
        if (user == null) return;

        if (getOutcome != null && !getOutcome.isEmpty()) {
            System.out.printf("%-20s %-15s %-15s %-15s%n",
                    "Категория", "Бюджет", "Расходы", "Остаток");
            System.out.println("-".repeat(65));

            getOutcome.forEach((k, v) -> {
                double budget = user.getWallet().getBudget(k) != null
                        ? user.getWallet().getBudget(k) : 0.0;
                double remaining = balanceService.getBudgetCategory(user, k);
                System.out.printf("%-20s %-15.2f %-15.2f %-15.2f",
                        k, budget, v, remaining);

                // Показываем предупреждения
                if (balanceService.budgetOverLimit(user, k)) {
                    System.out.print(" ⚠️ ПЕРЕРАСХОД!");
                } else if (balanceService.budgetIsZero(user, k)) {
                    System.out.print(" ⚠️ Бюджет исчерпан!");
                } else if (balanceService.budgetOverLimitPercent(user, k, LIMIT_PERCENTAGE)) {
                    System.out.print(" ⚠️ Осталось менее 20% бюджета!");
                }
                System.out.println();
            });
        } else {
            System.out.println("Нет данных о расходах.");
        }
    }

    /**
     * Shows available categories.
     */
    public void showCategories() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("Ошибка: пользователь не авторизован!");
            return;
        }

        List<String> allCategories = balanceService.getAllCategories(user);
        List<String> budgetCategories = balanceService.getBudgetCategories(user);

        System.out.println("\n" + "=".repeat(40));
        System.out.println("      ДОСТУПНЫЕ КАТЕГОРИИ");
        System.out.println("=".repeat(40));

        if (!allCategories.isEmpty()) {
            System.out.println("Все категории транзакций:");
            allCategories.forEach(cat -> System.out.println("  • " + cat));
        } else {
            System.out.println("Категории транзакций отсутствуют.");
        }

        System.out.println();

        if (!budgetCategories.isEmpty()) {
            System.out.println("Категории с установленным бюджетом:");
            budgetCategories.forEach(cat -> {
                double budget = user.getWallet().getBudget(cat);
                System.out.printf("  • %s (бюджет: %.2f)%n", cat, budget);
            });
        } else {
            System.out.println("Бюджеты не установлены.");
        }
        System.out.println("=".repeat(40));
    }

    /**
     * Prints help information for all features.
     */
    public void printHelp() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                   СПРАВКА");
        System.out.println("=".repeat(60) + "\n");

        // --- Транзакции ---
        System.out.println("📊 ТРАНЗАКЦИИ:");
        System.out.println("  " + Help.TRANSACTION_OPERATION.getDescription());
        System.out.println("  " + Help.TRANSACTION_ADD.getDescription());
        System.out.println("  " + Help.TRANSACTION_REMOVE.getDescription());
        System.out.println("  " + Help.TRANSACTION_UPDATE.getDescription());
        System.out.println();

        // --- Бюджеты ---
        System.out.println("💰 БЮДЖЕТЫ:");
        System.out.println("  " + Help.BUDGET_OPERATION.getDescription());
        System.out.println("  " + Help.BUDGET_ADD.getDescription());
        System.out.println("  " + Help.BUDGET_REMOVE.getDescription());
        System.out.println();

        // --- Статистика ---
        System.out.println("📈 СТАТИСТИКА:");
        System.out.println("  " + Help.STATISTICS_OPERATION.getDescription());
        System.out.println("  " + Help.STATISTICS_ALL.getDescription());
        System.out.println("  " + Help.STATISTICS_BY_CATEGORY.getDescription());
        System.out.println();

        // --- Переводы ---
        System.out.println("🔄 ПЕРЕВОДЫ:");
        System.out.println("  " + Help.TRANSACTION_OPERATION_SEND_TO_USER.getDescription());
        System.out.println();

        // --- Управление пользователем ---
        System.out.println("👤 УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЕМ:");
        System.out.println("  " + Help.JSON_OPERATION.getDescription());
        System.out.println("  " + Help.JSON_UPLOAD.getDescription());
        System.out.println("  " + Help.JSON_UNLOAD.getDescription());
        System.out.println("  " + Help.DELETE_USER.getDescription());
        System.out.println();

        System.out.println("=".repeat(60));
        System.out.println("Для просмотра примеров использования выберите 'Примеры' в меню справки.");
    }

    /**
     * Prints usage examples for all features.
     */
    public void printExamples() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("           ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ");
        System.out.println("=".repeat(60) + "\n");

        // --- Транзакции ---
        System.out.println("📊 ТРАНЗАКЦИИ:");

        System.out.println("  Добавление дохода:");
        System.out.println("    " + Examples.ADD_INCOME.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Добавление расхода:");
        System.out.println("    " + Examples.ADD_OUTCOME.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Изменение транзакции:");
        System.out.println("    " + Examples.CHANGE_TRANSACTION.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Удаление транзакции:");
        System.out.println("    " + Examples.DELETE_TRANSACTION.getDescription().replace("\n", "\n    ") + "\n");

        // --- Бюджеты ---
        System.out.println("💰 БЮДЖЕТЫ:");

        System.out.println("  Добавление бюджета:");
        System.out.println("    " + Examples.BUDGET_ADD.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Удаление бюджета:");
        System.out.println("    " + Examples.BUDGET_REMOVE.getDescription().replace("\n", "\n    ") + "\n");

        // --- Статистика ---
        System.out.println("📈 СТАТИСТИКА:");

        System.out.println("  Полная статистика:");
        System.out.println("    " + Examples.STATS_FULL.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Статистика по категориям:");
        System.out.println("    " + Examples.STATS_BY_CATEGORY.getDescription().replace("\n", "\n    ") + "\n");

        // --- Перевод ---
        System.out.println("🔄 ПЕРЕВОД СРЕДСТВ:");

        System.out.println("  Пример перевода:");
        System.out.println("    " + Examples.TRANSFER_TO_USER.getDescription().replace("\n", "\n    ") + "\n");

        // --- Управление пользователем ---
        System.out.println("👤 УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЕМ:");

        System.out.println("  Загрузка JSON:");
        System.out.println("    " + Examples.JSON_LOAD.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Сохранение JSON:");
        System.out.println("    " + Examples.JSON_SAVE.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Удаление пользователя:");
        System.out.println("    " + Examples.DELETE_USER.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("=".repeat(60));
    }

    /**
     * Shows help menu.
     */
    public void showHelpMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        МЕНЮ СПРАВКИ");
        System.out.println("=".repeat(40));
        System.out.println("1. Показать справку по функциям");
        System.out.println("2. Показать примеры использования");
        System.out.println("3. Вернуться назад");
        System.out.println("=".repeat(40));
        System.out.print("Выберите действие (1-3): ");
    }
}