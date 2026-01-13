package com.business.services;

import com.business.entities.AccountHolder;
import com.infrastructure.InMemoryUserRepository;
import com.interf.DisplayService;

import java.util.Map;
import java.util.Scanner;

public class ApplicationController {
    private final AuthenticationService authenticationService;
    private final FinancialOperationsService financialOperationsService;
    private final Scanner scanner;
    private final DataPersistenceService dataPersistenceService;
    private final DisplayService displayService;
    private final UserInteractionHandler userInteractionHandler;

    public ApplicationController() {
        this.authenticationService = new AuthenticationService(new InMemoryUserRepository());
        this.financialOperationsService = new FinancialOperationsService();
        this.scanner = new Scanner(System.in);
        this.dataPersistenceService = new DataPersistenceService();
        this.displayService = new DisplayService(this.authenticationService, this.financialOperationsService);
        this.userInteractionHandler = new UserInteractionHandler(authenticationService, financialOperationsService, scanner, dataPersistenceService, displayService);

        // Устанавливаем обработчик завершения работы для сохранения данных
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nСохранение данных перед завершением...");
            dataPersistenceService.save(authenticationService.getUserMap());
        }));
    }

    // Метод запуска приложения
    public void start() {
        // Загрузка данных при старте
        loadInitialData();

        // Основной цикл приложения
        while (true) {
            try {
                if (authenticationService.isLoggedIn()) {
                    displayService.showMainMenu();
                    userInteractionHandler.handleMainMenu();
                } else {
                    displayService.showLoginMenu();
                    userInteractionHandler.handleLoginMenu();
                }
            } catch (Exception e) {
                System.err.println("❌ Произошла непредвиденная ошибка: " + e.getMessage());
                System.err.println("Попробуйте еще раз или перезапустите приложение.");
                e.printStackTrace();

                // Создаем резервную копию при ошибке
                dataPersistenceService.backupData(authenticationService.getUserMap());

                // Ожидаем ввод пользователя перед продолжением
                System.out.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
            }
        }
    }

    private void loadInitialData() {
        try {
            Map<String, AccountHolder> loadedUsers = dataPersistenceService.load();
            if (loadedUsers != null && !loadedUsers.isEmpty()) {
                authenticationService.setUserMap(loadedUsers);
            } else {
                System.out.println("ℹ️  Данные не найдены, начинаем с пустой базы");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Ошибка при загрузке данных: " + e.getMessage());
            System.err.println("Начинаем с пустой базы данных");
        }
    }
}