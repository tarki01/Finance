package com.business.services;

import com.business.entities.AccountHolder;
import com.business.entities.FinancialAccount;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DataPersistenceService {
    private static final Path DATA_FILE_PATH = Paths.get("users.data");
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    static {
        JSON_MAPPER.registerModule(new JavaTimeModule());
        JSON_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void save(Map<String, AccountHolder> users) {
        if (users == null || users.isEmpty()) {
            System.out.println("⚠️  Нет данных для сохранения.");
            return;
        }

        try {
            // Проверяем и создаем директорию, если нужно
            Path parentDir = DATA_FILE_PATH.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(DATA_FILE_PATH.toFile()))) {
                oos.writeObject(new HashMap<>(users)); // Создаем копию для безопасности
                System.out.println("✅ Данные сохранены в файл: " + DATA_FILE_PATH.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    public Map<String, AccountHolder> load() {
        if (!Files.exists(DATA_FILE_PATH)) {
            System.out.println("ℹ️  Файл данных не найден, будет создан новый.");
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(DATA_FILE_PATH.toFile()))) {
            @SuppressWarnings("unchecked")
            Map<String, AccountHolder> users = (Map<String, AccountHolder>) ois.readObject();
            System.out.println("✅ Данные загружены из файла: " + DATA_FILE_PATH.toAbsolutePath());
            return users;
        } catch (FileNotFoundException e) {
            System.err.println("❌ Файл данных не найден: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("❌ Ошибка чтения файла данных: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Неверный формат файла данных: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Неизвестная ошибка при загрузке данных: " + e.getMessage());
        }

        return new HashMap<>();
    }

    public void saveJSON(AccountHolder accountHolder, String filename) {
        if (accountHolder == null) {
            System.out.println("❌ Ошибка: пользователь не указан.");
            return;
        }

        if (filename == null || filename.trim().isEmpty()) {
            filename = accountHolder.getUsername() + ".json";
        }

        File file = new File(filename);
        try {
            JSON_MAPPER.writeValue(file, accountHolder);
            System.out.println("✅ Данные сохранены в JSON файл: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Ошибка при сохранении JSON: " + e.getMessage());
        }
    }

    public AccountHolder loadJSON(File file) {
        if (file == null || !file.exists()) {
            System.out.println("❌ Файл не найден: " +
                    (file != null ? file.getPath() : "null"));
            return null;
        }

        if (!file.getName().toLowerCase().endsWith(".json")) {
            System.out.println("❌ Файл должен быть в формате JSON.");
            return null;
        }

        try {
            AccountHolder accountHolder = JSON_MAPPER.readValue(file, AccountHolder.class);

            // Валидация загруженного пользователя
            if (accountHolder.getUsername() == null || accountHolder.getUsername().trim().isEmpty()) {
                System.out.println("❌ Неверный формат JSON: отсутствует имя пользователя.");
                return null;
            }

            if (accountHolder.getFinancialAccount() == null) {
                accountHolder.setFinancialAccount(new FinancialAccount());
                System.out.println("⚠️  Кошелек пользователя не найден, создан новый.");
            }

            System.out.println("✅ JSON файл успешно загружен: " + file.getName());
            return accountHolder;
        } catch (IOException e) {
            System.err.println("❌ Ошибка при чтении JSON файла: " + e.getMessage());
            return null;
        }
    }

    public void backupData(Map<String, AccountHolder> users) {
        if (users == null || users.isEmpty()) {
            System.out.println("⚠️  Нет данных для резервного копирования.");
            return;
        }

        String backupFilename = "backup_users_" +
                System.currentTimeMillis() + ".data";
        File backupFile = new File(backupFilename);

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(backupFile))) {
            oos.writeObject(new HashMap<>(users));
            System.out.println("✅ Резервная копия создана: " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Ошибка при создании резервной копии: " + e.getMessage());
        }
    }
}