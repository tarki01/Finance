package com.interf;

import com.interf.enums.OperationDescriptions;
import com.interf.enums.UsageExamples;
import com.business.entities.FinancialEntry;
import com.business.entities.AccountHolder;
import com.business.services.FinancialOperationsService;
import com.business.services.AuthenticationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для отображения элементов пользовательского интерфейса и статистики.
 */
public class DisplayService {
    /**
     * Экземпляр сервиса аутентификации.
     */
    private AuthenticationService authenticationService;

    /**
     * Константа для процентного предела бюджета.
     */
    private static final int LIMIT_PERCENTAGE = 80;

    /**
     * Экземпляр сервиса финансовых операций.
     */
    private FinancialOperationsService financialOperationsService;

    /**
     * Строка формата для статистики.
     */
    private static final String REGEX_STATS = "%20s %n";

    /**
     * Форматтер даты для отображения.
     */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    /**
     * Конструктор для ShowcaseService.
     *
     * @param authenticationServiceParam сервис аутентификации
     * @param financialOperationsServiceParam сервис финансовых операций
     */
    public DisplayService(final AuthenticationService authenticationServiceParam,
                          final FinancialOperationsService financialOperationsServiceParam) {
        this.authenticationService = authenticationServiceParam;
        this.financialOperationsService = financialOperationsServiceParam;
    }

    /**
     * Отображает меню входа.
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
     * Отображает главное меню.
     */
    public void showMainMenu() {
        AccountHolder currentAccountHolder = authenticationService.getCurrentAccountHolder();
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        ГЛАВНОЕ МЕНЮ");
        System.out.println("=".repeat(40));
        System.out.println("Текущий пользователь: " +
                (currentAccountHolder != null ? currentAccountHolder.getUsername() : "не авторизован"));
        if (currentAccountHolder != null) {
            System.out.printf("Текущий баланс: %.2f%n",
                    financialOperationsService.getCurrentBalance(currentAccountHolder));
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
     * Отображает меню операций с JSON.
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
     * Отображает меню операций с транзакциями.
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
     * Отображает опции редактирования транзакции.
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
     * Отображает все транзакции для текущего пользователя с пагинацией.
     */
    public void showAllTransactions(boolean showActions) {
        AccountHolder accountHolder = authenticationService.getCurrentAccountHolder();
        if (accountHolder == null) {
            System.out.println("Ошибка: пользователь не авторизован!");
            return;
        }

        List<FinancialEntry> financialEntries = accountHolder.getFinancialAccount().getFinancialEntries();
        if (financialEntries.isEmpty()) {
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
        for (FinancialEntry t : financialEntries) {
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
     * Отображает всю статистику для текущего пользователя.
     */
    public void showAllStatistic() {
        AccountHolder accountHolder = authenticationService.getCurrentAccountHolder();
        if (accountHolder == null) {
            System.out.println("Ошибка: пользователь не авторизован!");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("                  СТАТИСТИКА");
        System.out.println("=".repeat(60));
        System.out.println("\n--- Общие данные ---");
        System.out.printf("Общая сумма доходов: %.2f%n", financialOperationsService.getAllIncome(accountHolder));
        System.out.printf("Общая сумма расходов: %.2f%n", financialOperationsService.getAllOutcome(accountHolder));
        System.out.printf("Текущий баланс: %.2f%n", financialOperationsService.getCurrentBalance(accountHolder));

        if (financialOperationsService.outcomeOverIncomeAll(accountHolder)) {
            System.out.println("\n⚠️  ВНИМАНИЕ: Расходы превышают доходы!");
        }

        System.out.println("\n--- Бюджеты ---");
        printBudgets(accountHolder.getFinancialAccount().getBudgetsCategories());

        System.out.println("\n--- Доходы по категориям ---");
        printIncomes(financialOperationsService.getIncomeByCategory(accountHolder));

        System.out.println("\n--- Расходы по категориям ---");
        printOutcomes(financialOperationsService.getOutcomeByCategory(accountHolder));
        System.out.println("=".repeat(60));
    }

    /**
     * Отображает меню статистики.
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
     * Отображает статистику по категориям и периоду времени.
     *
     * @param firstTime начальное время
     * @param secondTime конечное время
     * @param categories массив категорий
     */
    public void showStatisticByCategory(final LocalDateTime firstTime,
                                        final LocalDateTime secondTime,
                                        final String[] categories) {
        try {
            AccountHolder accountHolder = authenticationService.getCurrentAccountHolder();
            if (accountHolder == null) {
                System.out.println("Ошибка: пользователь не авторизован!");
                return;
            }

            Set<String> categoriesSet = new TreeSet<>(Arrays.asList(categories));
            if (categoriesSet.isEmpty() ||
                    (categoriesSet.size() == 1 && categoriesSet.iterator().next().isEmpty())) {
                categoriesSet.addAll(accountHolder.getFinancialAccount().getBudgetsCategories().keySet());
            }

            if (categoriesSet.isEmpty()) {
                System.out.println("Категории не указаны и бюджеты не установлены.");
                System.out.println("Доступные категории: " + financialOperationsService.getAllCategories(accountHolder));
                return;
            }

            Map<String, Double> budgets = accountHolder.getFinancialAccount()
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

            List<FinancialEntry> list = financialOperationsService.getTransactionByCategories(
                    accountHolder, firstTime, secondTime, filteredCategories);

            if (list.isEmpty()) {
                System.out.println("\nНет транзакций по выбранным категориям в указанный период.");
            } else {
                Map<String, Double> mapOfIncomes = list.stream()
                        .filter(FinancialEntry::getIsIncome)
                        .collect(Collectors.toMap(
                                FinancialEntry::getCategory,
                                FinancialEntry::getAmount,
                                Double::sum,
                                TreeMap::new
                        ));
                if (!mapOfIncomes.isEmpty()) {
                    printIncomes(mapOfIncomes);
                }

                Map<String, Double> mapOfOutcomes = list.stream()
                        .filter(t -> !t.getIsIncome())
                        .collect(Collectors.toMap(
                                FinancialEntry::getCategory,
                                FinancialEntry::getAmount,
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
     * Выводит статистику доходов.
     *
     * @param getIncome карта категорий к суммам доходов
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
     * Выводит информацию о бюджетах.
     *
     * @param budgets карта категорий к суммам бюджетов
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
     * Выводит статистику расходов с информацией о бюджетах.
     *
     * @param getOutcome карта категорий к суммам расходов
     */
    public void printOutcomes(final Map<String, Double> getOutcome) {
        AccountHolder accountHolder = authenticationService.getCurrentAccountHolder();
        if (accountHolder == null) return;

        if (getOutcome != null && !getOutcome.isEmpty()) {
            System.out.printf("%-20s %-15s %-15s %-15s%n",
                    "Категория", "Бюджет", "Расходы", "Остаток");
            System.out.println("-".repeat(65));

            getOutcome.forEach((k, v) -> {
                double budget = accountHolder.getFinancialAccount().getBudget(k) != null
                        ? accountHolder.getFinancialAccount().getBudget(k) : 0.0;
                double remaining = financialOperationsService.getBudgetCategory(accountHolder, k);
                System.out.printf("%-20s %-15.2f %-15.2f %-15.2f",
                        k, budget, v, remaining);

                // Показываем предупреждения
                if (financialOperationsService.budgetOverLimit(accountHolder, k)) {
                    System.out.print(" ⚠️ ПЕРЕРАСХОД!");
                } else if (financialOperationsService.budgetIsZero(accountHolder, k)) {
                    System.out.print(" ⚠️ Бюджет исчерпан!");
                } else if (financialOperationsService.budgetOverLimitPercent(accountHolder, k, LIMIT_PERCENTAGE)) {
                    System.out.print(" ⚠️ Осталось менее 20% бюджета!");
                }
                System.out.println();
            });
        } else {
            System.out.println("Нет данных о расходах.");
        }
    }

    /**
     * Показывает доступные категории.
     */
    public void showCategories() {
        AccountHolder accountHolder = authenticationService.getCurrentAccountHolder();
        if (accountHolder == null) {
            System.out.println("Ошибка: пользователь не авторизован!");
            return;
        }

        List<String> allCategories = financialOperationsService.getAllCategories(accountHolder);
        List<String> budgetCategories = financialOperationsService.getBudgetCategories(accountHolder);

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
                double budget = accountHolder.getFinancialAccount().getBudget(cat);
                System.out.printf("  • %s (бюджет: %.2f)%n", cat, budget);
            });
        } else {
            System.out.println("Бюджеты не установлены.");
        }
        System.out.println("=".repeat(40));
    }

    /**
     * Выводит справочную информацию по всем функциям.
     */
    public void printHelp() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                   СПРАВКА");
        System.out.println("=".repeat(60) + "\n");

        // --- Транзакции ---
        System.out.println("📊 ТРАНЗАКЦИИ:");
        System.out.println("  " + OperationDescriptions.TRANSACTION_OPERATION.getDescription());
        System.out.println("  " + OperationDescriptions.TRANSACTION_ADD.getDescription());
        System.out.println("  " + OperationDescriptions.TRANSACTION_REMOVE.getDescription());
        System.out.println("  " + OperationDescriptions.TRANSACTION_UPDATE.getDescription());
        System.out.println();

        // --- Бюджеты ---
        System.out.println("💰 БЮДЖЕТЫ:");
        System.out.println("  " + OperationDescriptions.BUDGET_OPERATION.getDescription());
        System.out.println("  " + OperationDescriptions.BUDGET_ADD.getDescription());
        System.out.println("  " + OperationDescriptions.BUDGET_REMOVE.getDescription());
        System.out.println();

        // --- Статистика ---
        System.out.println("📈 СТАТИСТИКА:");
        System.out.println("  " + OperationDescriptions.STATISTICS_OPERATION.getDescription());
        System.out.println("  " + OperationDescriptions.STATISTICS_ALL.getDescription());
        System.out.println("  " + OperationDescriptions.STATISTICS_BY_CATEGORY.getDescription());
        System.out.println();

        // --- Переводы ---
        System.out.println("🔄 ПЕРЕВОДЫ:");
        System.out.println("  " + OperationDescriptions.TRANSACTION_OPERATION_SEND_TO_USER.getDescription());
        System.out.println();

        // --- Управление пользователем ---
        System.out.println("👤 УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЕМ:");
        System.out.println("  " + OperationDescriptions.JSON_OPERATION.getDescription());
        System.out.println("  " + OperationDescriptions.JSON_UPLOAD.getDescription());
        System.out.println("  " + OperationDescriptions.JSON_UNLOAD.getDescription());
        System.out.println("  " + OperationDescriptions.DELETE_USER.getDescription());
        System.out.println();

        System.out.println("=".repeat(60));
        System.out.println("Для просмотра примеров использования выберите 'Примеры' в меню справки.");
    }

    /**
     * Выводит примеры использования для всех функций.
     */
    public void printExamples() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("           ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ");
        System.out.println("=".repeat(60) + "\n");

        // --- Транзакции ---
        System.out.println("📊 ТРАНЗАКЦИИ:");

        System.out.println("  Добавление дохода:");
        System.out.println("    " + UsageExamples.ADD_INCOME.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Добавление расхода:");
        System.out.println("    " + UsageExamples.ADD_OUTCOME.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Изменение транзакции:");
        System.out.println("    " + UsageExamples.CHANGE_TRANSACTION.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Удаление транзакции:");
        System.out.println("    " + UsageExamples.DELETE_TRANSACTION.getDescription().replace("\n", "\n    ") + "\n");

        // --- Бюджеты ---
        System.out.println("💰 БЮДЖЕТЫ:");

        System.out.println("  Добавление бюджета:");
        System.out.println("    " + UsageExamples.BUDGET_ADD.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Удаление бюджета:");
        System.out.println("    " + UsageExamples.BUDGET_REMOVE.getDescription().replace("\n", "\n    ") + "\n");

        // --- Статистика ---
        System.out.println("📈 СТАТИСТИКА:");

        System.out.println("  Полная статистика:");
        System.out.println("    " + UsageExamples.STATS_FULL.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Статистика по категориям:");
        System.out.println("    " + UsageExamples.STATS_BY_CATEGORY.getDescription().replace("\n", "\n    ") + "\n");

        // --- Перевод ---
        System.out.println("🔄 ПЕРЕВОД СРЕДСТВ:");

        System.out.println("  Пример перевода:");
        System.out.println("    " + UsageExamples.TRANSFER_TO_USER.getDescription().replace("\n", "\n    ") + "\n");

        // --- Управление пользователем ---
        System.out.println("👤 УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЕМ:");

        System.out.println("  Загрузка JSON:");
        System.out.println("    " + UsageExamples.JSON_LOAD.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Сохранение JSON:");
        System.out.println("    " + UsageExamples.JSON_SAVE.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("  Удаление пользователя:");
        System.out.println("    " + UsageExamples.DELETE_USER.getDescription().replace("\n", "\n    ") + "\n");

        System.out.println("=".repeat(60));
    }

    /**
     * Показывает меню справки.
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