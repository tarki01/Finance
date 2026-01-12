package com.core.service;

import com.cli.ShowcaseService;
import com.core.exception.CategoryNotFound;
import com.core.exception.PasswordNotFoundException;
import com.core.exception.UserAlreadyCreatedException;
import com.core.exception.UserNotFoundException;
import com.core.model.Transaction;
import com.core.model.User;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class HandleService {
    private final LoginService loginService;
    private final BalanceService balanceService;
    private final Scanner scanner;
    private final FileService fileService;
    private final ShowcaseService showcaseService;

    public HandleService(LoginService loginService, BalanceService balanceService,
                         Scanner scanner, FileService fileService, ShowcaseService showcaseService) {
        this.loginService = loginService;
        this.balanceService = balanceService;
        this.scanner = scanner;
        this.fileService = fileService;
        this.showcaseService = showcaseService;
    }

    // Метод изображения меню регистрации
    public void handleLoginMenu() {
        String param = scanner.nextLine().trim();
        switch (param) {
            case "1":
                handleRegister();
                break;
            case "2":
                handleLogin();
                break;
            case "3":
                handleExit();
                break;
            case "4":
                handleLoadJson();
                break;
            case "5":
                showcaseService.showHelpMenu();
                handleHelpMenu();
                break;
            default:
                System.out.println("❌ Некорректный ввод. Используйте цифры от 1 до 5.");
        }
    }

    private void handleHelpMenu() {
        String param = scanner.nextLine().trim();
        switch (param) {
            case "1":
                showcaseService.printHelp();
                break;
            case "2":
                showcaseService.printExamples();
                break;
            case "3":
                break;
            default:
                System.out.println("❌ Некорректный ввод.");
        }
    }

    private void handleExit() {
        System.out.print("Сохранить данные перед выходом? (Y/N): ");
        String answer = scanner.nextLine().trim().toUpperCase();
        if (answer.equals("Y") || answer.equals("ДА")) {
            fileService.save(loginService.getUserMap());
            System.out.println("✅ Данные сохранены.");
        }
        System.out.println("👋 До свидания!");
        System.exit(0);
    }

    // Метод обработки логирования
    public boolean handleLogin() {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine().trim();

        if (login.isEmpty()) {
            System.out.println("❌ Логин не может быть пустым.");
            return false;
        }

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        if (password.isEmpty()) {
            System.out.println("❌ Пароль не может быть пустым.");
            return false;
        }

        try {
            loginService.login(login, password);
            System.out.println("✅ Вы успешно вошли в систему!");
            return true;
        } catch (UserNotFoundException e) {
            System.out.println("❌ Пользователь не найден! Попробуйте другой аккаунт или зарегистрируйтесь.");
        } catch (PasswordNotFoundException e) {
            System.out.println("❌ Неверный пароль! Попробуйте еще раз.");
        }
        return false;
    }

    public void handleRegister() {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine().trim();

        if (login.isEmpty()) {
            System.out.println("❌ Логин не может быть пустым.");
            return;
        }

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        if (password.isEmpty()) {
            System.out.println("❌ Пароль не может быть пустым.");
            return;
        }

        try {
            loginService.registration(login, password);
            System.out.println("✅ Вы успешно зарегистрировались!");
        } catch (UserAlreadyCreatedException e) {
            System.out.println("❌ Такой пользователь уже существует!");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Метод обработки главного меню
    public void handleMainMenu() {
        String param = scanner.nextLine().trim();
        switch (param) {
            case "1":
                handleTransactionMenu();
                break;
            case "2":
                handleBudgetCategory();
                break;
            case "3":
                handleStatistic();
                break;
            case "4":
                handleTransaction();
                break;
            case "5":
                handleLogout();
                break;
            case "6":
                handleJsons();
                break;
            case "7":
                handleHelp();
                break;
            case "8":
                showcaseService.showAllTransactions(false);
                System.out.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
                break;
            default:
                System.out.println("❌ Вы ввели ошибочное значение. Повторите ещё раз");
        }
    }

    private void handleLogout() {
        loginService.unLogin();
        System.out.println("✅ Вы вышли из аккаунта.");
    }

    private void handleHelp() {
        showcaseService.showHelpMenu();
        String help = scanner.nextLine().trim();
        switch (help) {
            case "1":
                showcaseService.printHelp();
                break;
            case "2":
                showcaseService.printExamples();
                break;
            case "3":
                break;
            default:
                System.out.println("❌ Некорректный ввод.");
        }
    }

    private void handleJsons() {
        showcaseService.showJsons();
        String param = scanner.nextLine().trim();
        switch (param) {
            case "1":
                handleLoadJson();
                break;
            case "2":
                handleSaveJson();
                break;
            case "3":
                handleDeleteUser();
                break;
            case "4":
                break;
            default:
                System.out.println("❌ Некорректный ввод.");
        }
    }

    private void handleDeleteUser() {
        System.out.print("⚠️  Удалить текущий аккаунт и все данные? (Y/N): ");
        switch (scanner.nextLine().trim().toUpperCase()) {
            case "Y":
            case "ДА":
                String username = loginService.getCurrentUser().getUsername();
                loginService.getUserMap().remove(username);
                loginService.unLogin();
                System.out.println("✅ Пользователь '" + username + "' успешно удален.");
                break;
            case "N":
            case "НЕТ":
                System.out.println("Удаление отменено.");
                break;
            default:
                System.out.println("❌ Введите Y (Да) или N (Нет).");
        }
    }

    private void handleSaveJson() {
        try {
            User currentUser = loginService.getCurrentUser();
            if (currentUser == null) {
                System.out.println("❌ Ошибка: пользователь не авторизован!");
                return;
            }

            System.out.print("Введите имя файла для сохранения (без расширения): ");
            String filename = scanner.nextLine().trim();
            if (filename.isEmpty()) {
                filename = currentUser.getUsername();
            }

            fileService.saveJSON(currentUser, filename + ".json");
            System.out.println("✅ Данные пользователя сохранены в файл: " + filename + ".json");
        } catch (Exception e) {
            System.out.println("❌ Ошибка при сохранении: " + e.getMessage());
        }
    }

    private void handleLoadJson() {
        System.out.print("Введите имя файла для загрузки (с расширением .json): ");
        String filename = scanner.nextLine().trim();

        if (!filename.toLowerCase().endsWith(".json")) {
            filename += ".json";
        }

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("❌ Файл '" + filename + "' не найден.");
            return;
        }

        try {
            User loadedUser = fileService.loadJSON(file);
            if (loadedUser == null) {
                System.out.println("❌ Ошибка загрузки файла.");
                return;
            }

            Map<String, User> userMap = loginService.getUserMap();
            if (userMap.containsKey(loadedUser.getUsername())) {
                System.out.print("Пользователь '" + loadedUser.getUsername() +
                        "' уже существует. Перезаписать? (Y/N): ");
                String answer = scanner.nextLine().trim().toUpperCase();
                if (answer.equals("Y") || answer.equals("ДА")) {
                    userMap.put(loadedUser.getUsername(), loadedUser);
                    System.out.println("✅ Данные пользователя обновлены.");

                    // Если это текущий пользователь, обновляем его
                    if (loginService.getCurrentUser() != null &&
                            loginService.getCurrentUser().getUsername().equals(loadedUser.getUsername())) {
                        loginService.setCurrentUser(loadedUser);
                    }
                } else {
                    System.out.println("Загрузка отменена.");
                }
            } else {
                userMap.put(loadedUser.getUsername(), loadedUser);
                System.out.println("✅ Пользователь '" + loadedUser.getUsername() + "' успешно добавлен.");
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    // Метод добавления дохода
    public void handleAddIncome() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("❌ Ошибка: пользователь не авторизован!");
            return;
        }

        System.out.print("Введите категорию дохода: ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) {
            System.out.println("❌ Категория не может быть пустой.");
            return;
        }

        System.out.print("Введите сумму дохода: ");
        String amountInput = scanner.nextLine().trim();

        try {
            double amount = Double.parseDouble(amountInput);
            balanceService.addIncome(user, category, amount);
            System.out.printf("✅ Добавлен доход: %s - %.2f%n", category, amount);
        } catch (NumberFormatException e) {
            System.out.println("❌ Ошибка: введите корректное число.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    public void handleTransactionMenu() {
        showcaseService.showTransactionMenu();
        String param = scanner.nextLine().trim();
        switch (param) {
            case "1":
                handleAddIncome();
                break;
            case "2":
                handleAddOutcome();
                break;
            case "3":
                showcaseService.showAllTransactions(false);
                System.out.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
                break;
            case "4":
                handleShowcaseTransaction();
                break;
            case "5":
                handleRemoveTransaction();
                break;
            case "6":
                break;
            default:
                System.out.println("❌ Введите подходящий вариант (1-6).");
        }
    }

    // Метод добавления расхода
    public void handleAddOutcome() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("❌ Ошибка: пользователь не авторизован!");
            return;
        }

        System.out.print("Введите категорию расхода: ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) {
            System.out.println("❌ Категория не может быть пустой.");
            return;
        }

        System.out.print("Введите сумму расхода: ");
        String amountInput = scanner.nextLine().trim();

        try {
            double amount = Double.parseDouble(amountInput);
            balanceService.addOutcome(user, category, amount);
            System.out.printf("✅ Добавлен расход: %s - %.2f%n", category, amount);

            // Проверка бюджета после добавления расхода
            if (user.getWallet().getBudget(category) != null) {
                double remaining = balanceService.getBudgetCategory(user, category);
                if (remaining < 0) {
                    System.out.println("⚠️  ВНИМАНИЕ: Превышен бюджет по категории '" + category + "'!");
                } else if (remaining == 0) {
                    System.out.println("⚠️  ВНИМАНИЕ: Бюджет по категории '" + category + "' исчерпан!");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Ошибка: введите корректное число.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Метод настройки начального баланса
    public void handleBudgetCategory() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("       УПРАВЛЕНИЕ БЮДЖЕТОМ");
        System.out.println("=".repeat(40));
        System.out.println("1. Установить бюджет");
        System.out.println("2. Удалить бюджет");
        System.out.println("3. Показать все бюджеты");
        System.out.println("4. Вернуться назад");
        System.out.println("=".repeat(40));
        System.out.print("Выберите действие (1-4): ");

        String param = scanner.nextLine().trim();
        switch (param) {
            case "1":
                handleSetBudget();
                break;
            case "2":
                handleRemoveBudget();
                break;
            case "3":
                showcaseService.showCategories();
                System.out.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
                break;
            case "4":
                break;
            default:
                System.out.println("❌ Введите подходящий вариант (1-4).");
        }
    }

    private void handleChangeTransaction() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("❌ Ошибка: пользователь не авторизован!");
            return;
        }

        List<Transaction> transactions = user.getWallet().getTransactions();
        if (transactions.isEmpty()) {
            System.out.println("❌ Список транзакций пуст!");
            return;
        }

        try {
            showcaseService.showAllTransactions(false);
            System.out.print("\nВведите номер транзакции для изменения: ");
            String transactionInput = scanner.nextLine().trim();

            int transactionIndex = Integer.parseInt(transactionInput) - 1;
            if (transactionIndex < 0 || transactionIndex >= transactions.size()) {
                System.out.println("❌ Неверный номер транзакции.");
                return;
            }

            Transaction transaction = transactions.get(transactionIndex);

            while (true) {
                showcaseService.showChangeTransaction();
                String param = scanner.nextLine().trim();

                switch (param) {
                    case "1":
                        System.out.print("Введите новое название категории: ");
                        String name = scanner.nextLine().trim();
                        if (name.isEmpty()) {
                            System.out.println("❌ Категория не может быть пустой.");
                        } else {
                            transaction.setCategory(name);
                            System.out.println("✅ Категория изменена.");
                        }
                        break;
                    case "2":
                        System.out.print("Введите новую сумму: ");
                        String amountInput = scanner.nextLine().trim();
                        try {
                            double amount = Double.parseDouble(amountInput);
                            if (amount <= 0) {
                                System.out.println("❌ Сумма должна быть положительной.");
                            } else {
                                transaction.setAmount(amount);
                                System.out.println("✅ Сумма изменена.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Введите корректное число.");
                        }
                        break;
                    case "3":
                        System.out.print("Введите новый тип (доход/расход): ");
                        String type = scanner.nextLine().trim().toLowerCase();
                        if (type.equals("доход")) {
                            transaction.setIsIncome(true);
                            System.out.println("✅ Тип изменен на 'доход'.");
                        } else if (type.equals("расход")) {
                            transaction.setIsIncome(false);
                            System.out.println("✅ Тип изменен на 'расход'.");
                        } else {
                            System.out.println("❌ Введите 'доход' или 'расход'.");
                        }
                        break;
                    case "4":
                        return;
                    default:
                        System.out.println("❌ Введите корректный вариант (1-4).");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Введите корректный номер.");
        }
    }

    private void handleRemoveTransaction() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("❌ Ошибка: пользователь не авторизован!");
            return;
        }

        List<Transaction> transactions = user.getWallet().getTransactions();
        if (transactions.isEmpty()) {
            System.out.println("❌ Список транзакций пуст!");
            return;
        }

        try {
            showcaseService.showAllTransactions(false);
            System.out.print("\nВведите номер транзакции для удаления: ");
            String transactionInput = scanner.nextLine().trim();

            int transactionIndex = Integer.parseInt(transactionInput) - 1;
            if (transactionIndex < 0 || transactionIndex >= transactions.size()) {
                System.out.println("❌ Неверный номер транзакции.");
                return;
            }

            Transaction removed = transactions.remove(transactionIndex);
            System.out.printf("✅ Транзакция №%d удалена: %s - %.2f%n",
                    (transactionIndex + 1), removed.getCategory(), removed.getAmount());

        } catch (NumberFormatException e) {
            System.out.println("❌ Введите корректный номер.");
        }
    }

    private void handleRemoveBudget() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("❌ Ошибка: пользователь не авторизован!");
            return;
        }

        try {
            System.out.print("Введите категорию бюджета для удаления: ");
            String category = scanner.nextLine().trim();

            if (category.isEmpty()) {
                System.out.println("❌ Категория не может быть пустой.");
                return;
            }

            balanceService.removeBudget(user, category);
            System.out.println("✅ Бюджет категории '" + category + "' удален.");

        } catch (CategoryNotFound e) {
            System.out.println("❌ " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    public void handleSetBudget() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("❌ Ошибка: пользователь не авторизован!");
            return;
        }

        try {
            System.out.print("Введите категорию бюджета: ");
            String category = scanner.nextLine().trim();

            if (category.isEmpty()) {
                System.out.println("❌ Категория не может быть пустой.");
                return;
            }

            System.out.print("Введите сумму бюджета: ");
            String amountInput = scanner.nextLine().trim();

            double amount = Double.parseDouble(amountInput);
            balanceService.setBudget(user, category, amount);
            System.out.printf("✅ Бюджет для категории '%s' установлен: %.2f%n", category, amount);

        } catch (NumberFormatException e) {
            System.out.println("❌ Введите корректное число.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Метод демонстрации статистики
    public void handleShowcaseTransaction() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("❌ Ошибка: пользователь не авторизован!");
            return;
        }

        try {
            showcaseService.showAllTransactions(true);
            String param = scanner.nextLine().trim();
            switch (param) {
                case "1":
                    handleRemoveTransaction();
                    break;
                case "2":
                    handleChangeTransaction();
                    break;
                case "3":
                    break;
                default:
                    System.out.println("❌ Введите корректный вариант (1-3).");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Введите корректный номер.");
        }
    }

    public void handleTransaction() {
        User user = loginService.getCurrentUser();
        if (user == null) {
            System.out.println("❌ Ошибка: пользователь не авторизован!");
            return;
        }

        try {
            System.out.print("Введите имя пользователя для перевода: ");
            String username = scanner.nextLine().trim();

            if (username.isEmpty()) {
                System.out.println("❌ Имя пользователя не может быть пустым.");
                return;
            }

            if (username.equals(user.getUsername())) {
                System.out.println("❌ Нельзя перевести деньги самому себе.");
                return;
            }

            User recipient = loginService.getUserMap().get(username);
            if (recipient == null) {
                System.out.println("❌ Пользователь '" + username + "' не найден.");
                return;
            }

            System.out.print("Введите сумму перевода: ");
            String amountInput = scanner.nextLine().trim();
            double amount = Double.parseDouble(amountInput);

            if (amount <= 0) {
                System.out.println("❌ Сумма должна быть положительной.");
                return;
            }

            double currentBalance = balanceService.getCurrentBalance(user);
            if (amount > currentBalance) {
                System.out.printf("❌ Недостаточно средств. Доступно: %.2f%n", currentBalance);
                return;
            }

            System.out.print("Введите описание перевода (необязательно): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) {
                description = "Перевод пользователю " + username;
            }

            // Выполняем перевод
            balanceService.addOutcome(user, description, amount);
            balanceService.addIncome(recipient, "Перевод от " + user.getUsername(), amount);

            System.out.printf("✅ Успешно переведено %.2f пользователю '%s'%n", amount, username);
            System.out.printf("Ваш новый баланс: %.2f%n", balanceService.getCurrentBalance(user));

        } catch (NumberFormatException e) {
            System.out.println("❌ Введите корректное число.");
        }
    }

    public void handleStatistic() {
        showcaseService.showStatistic();
        String cases = scanner.nextLine().trim();
        switch (cases) {
            case "1":
                showcaseService.showAllStatistic();
                break;
            case "2":
                handleStatisticByCategory();
                break;
            case "3":
                showcaseService.showCategories();
                break;
            case "4":
                break;
            default:
                System.out.println("❌ Введите корректный вариант (1-4).");
        }
    }

    private void handleStatisticByCategory() {
        LocalDateTime firstTime = null;
        LocalDateTime secondTime = null;

        try {
            System.out.println("Введите дату начала периода (формат: yyyy.MM.dd HH:mm:ss)");
            System.out.println("Например: 2024.01.01 00:00:00");
            System.out.println("Оставьте пустым для начала всех записей");
            System.out.print("Дата начала: ");
            String timeFrom = scanner.nextLine().trim();

            if (!timeFrom.isEmpty()) {
                firstTime = LocalDateTime.parse(timeFrom,
                        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
            } else {
                firstTime = LocalDateTime.MIN;
            }

            System.out.println("\nВведите дату окончания периода (формат: yyyy.MM.dd HH:mm:ss)");
            System.out.println("Например: 2024.12.31 23:59:59");
            System.out.println("Оставьте пустым для конца всех записей");
            System.out.print("Дата окончания: ");
            String timeTo = scanner.nextLine().trim();

            if (!timeTo.isEmpty()) {
                secondTime = LocalDateTime.parse(timeTo,
                        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
            } else {
                secondTime = LocalDateTime.MAX;
            }

            if (firstTime.isAfter(secondTime)) {
                System.out.println("⚠️  Дата начала позже даты окончания. Поменяю их местами.");
                LocalDateTime temp = firstTime;
                firstTime = secondTime;
                secondTime = temp;
            }

        } catch (DateTimeParseException e) {
            System.out.println("❌ Неверный формат даты! Используются стандартные значения.");
            firstTime = LocalDateTime.MIN;
            secondTime = LocalDateTime.MAX;
        }

        System.out.println("\nВведите категории через запятую (например: Еда, Транспорт, Развлечения)");
        System.out.println("Оставьте пустым для выбора всех категорий");
        System.out.print("Категории: ");
        String categoriesInput = scanner.nextLine().trim();

        String[] categories = categoriesInput.isEmpty() ?
                new String[0] : categoriesInput.split(",\\s*");

        showcaseService.showStatisticByCategory(firstTime, secondTime, categories);
    }
}