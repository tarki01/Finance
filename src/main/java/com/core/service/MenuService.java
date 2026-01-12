package com.core.service;

import com.cli.ShowcaseService;
import com.core.model.User;
import com.infra.MemoryUserRepositoryImpl;

import java.util.Map;
import java.util.Scanner;

public class MenuService {
    private final LoginService loginService;
    private final BalanceService balanceService;
    private final Scanner scanner;
    private final FileService fileService;
    private final ShowcaseService showcaseService;
    private final HandleService handleService;

    public MenuService() {
        this.loginService = new LoginService(new MemoryUserRepositoryImpl());
        this.balanceService = new BalanceService();
        this.scanner = new Scanner(System.in);
        this.fileService = new FileService();
        this.showcaseService = new ShowcaseService(this.loginService, this.balanceService);
        this.handleService = new HandleService(loginService, balanceService, scanner, fileService, showcaseService);

        // Устанавливаем обработчик завершения работы для сохранения данных
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nСохранение данных перед завершением...");
            fileService.save(loginService.getUserMap());
        }));
    }

    // Метод запуска приложения
    public void start() {
        printWelcomeMessage();

        // Загрузка данных при старте
        loadInitialData();

        // Основной цикл приложения
        while (true) {
            try {
                if (loginService.isLoggedIn()) {
                    showcaseService.showMainMenu();
                    handleService.handleMainMenu();
                } else {
                    showcaseService.showLoginMenu();
                    handleService.handleLoginMenu();
                }
            } catch (Exception e) {
                System.err.println("❌ Произошла непредвиденная ошибка: " + e.getMessage());
                System.err.println("Попробуйте еще раз или перезапустите приложение.");
                e.printStackTrace();

                // Создаем резервную копию при ошибке
                fileService.backupData(loginService.getUserMap());

                // Ожидаем ввод пользователя перед продолжением
                System.out.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
            }
        }
    }

    private void printWelcomeMessage() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("          ПРИЛОЖЕНИЕ ДЛЯ КОНТРОЛЯ ЛИЧНЫХ ФИНАНСОВ");
        System.out.println("=".repeat(60));
        System.out.println("Управляйте доходами, расходами и бюджетами");
        System.out.println("Анализируйте статистику и планируйте финансы");
        System.out.println("=".repeat(60));
    }

    private void loadInitialData() {
        System.out.println("\nЗагрузка данных...");
        try {
            Map<String, User> loadedUsers = fileService.load();
            if (loadedUsers != null && !loadedUsers.isEmpty()) {
                loginService.setUserMap(loadedUsers);
                System.out.printf("✅ Загружено %d пользователь(ей)%n", loadedUsers.size());
            } else {
                System.out.println("ℹ️  Данные не найдены, начинаем с пустой базы");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Ошибка при загрузке данных: " + e.getMessage());
            System.err.println("Начинаем с пустой базы данных");
        }

        System.out.println("\nДля начала работы выберите действие из меню.");
        System.out.println("Для получения справки нажмите 5 в меню входа или 7 в главном меню.");
        System.out.println("=".repeat(60));
    }
}