import com.business.entities.FinancialEntry;
import com.business.entities.AccountHolder;
import com.business.services.DataPersistenceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DataPersistenceServiceTest {

    private DataPersistenceService dataPersistenceService;
    private AccountHolder testAccountHolder;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        dataPersistenceService = new DataPersistenceService();
        testAccountHolder = new AccountHolder("testUser", "password123");
        testAccountHolder.getFinancialAccount().addTransaction(new FinancialEntry(100.0, "salary", true));
        testAccountHolder.getFinancialAccount().addTransaction(new FinancialEntry(50.0, "food", false));
        testAccountHolder.getFinancialAccount().setBudget("food", 300.0);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Очистка созданных файлов после каждого теста
        Files.deleteIfExists(Path.of("users.data"));
        Files.deleteIfExists(Path.of("testUser.json"));
        Files.deleteIfExists(Path.of("nonexistent.json"));
        Files.deleteIfExists(Path.of("completeUser.json"));
        Files.deleteIfExists(Path.of("budgetUser.json"));
        Files.deleteIfExists(Path.of("emptyUser.json"));
        Files.deleteIfExists(Path.of("timestampUser.json"));
        Files.deleteIfExists(Path.of("corrupted.json"));
        Files.deleteIfExists(Path.of("backup_users_0.data"));
    }

    @Test
    @DisplayName("Сохранение и загрузка пустой Map пользователей")
    public void saveAndLoadEmptyMapTest() {
        // Given
        Map<String, AccountHolder> emptyMap = new HashMap<>();

        // When
        dataPersistenceService.save(emptyMap);
        Map<String, AccountHolder> loadedMap = dataPersistenceService.load();

        // Then
        assertNotNull(loadedMap);
        assertTrue(loadedMap.isEmpty());
    }

    @Test
    @DisplayName("Сохранение и загрузка Map с несколькими пользователями")
    public void saveAndLoadMultipleUsersTest() {
        // Given
        Map<String, AccountHolder> users = new HashMap<>();
        AccountHolder accountHolder1 = new AccountHolder("user1", "pass1");
        AccountHolder accountHolder2 = new AccountHolder("user2", "pass2");

        accountHolder1.getFinancialAccount().addTransaction(new FinancialEntry(100.0, "income", true));
        accountHolder2.getFinancialAccount().addTransaction(new FinancialEntry(200.0, "expense", false));

        users.put("user1", accountHolder1);
        users.put("user2", accountHolder2);

        // When
        dataPersistenceService.save(users);
        Map<String, AccountHolder> loadedUsers = dataPersistenceService.load();

        // Then
        assertNotNull(loadedUsers);
        assertEquals(2, loadedUsers.size());
        assertTrue(loadedUsers.containsKey("user1"));
        assertTrue(loadedUsers.containsKey("user2"));
        assertEquals(100.0, loadedUsers.get("user1").getFinancialAccount().getFinancialEntries().get(0).getAmount(), 0);
        assertEquals(200.0, loadedUsers.get("user2").getFinancialAccount().getFinancialEntries().get(0).getAmount(), 0);
    }

    @Test
    @DisplayName("Загрузка из несуществующего файла должна возвращать пустую Map")
    public void loadNonExistentFileTest() {
        // Удаляем файл если существует
        try {
            Files.deleteIfExists(Path.of("users.data"));
        } catch (IOException e) {
            // Игнорируем
        }

        // When
        Map<String, AccountHolder> result = dataPersistenceService.load();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Перезапись существующего файла при сохранении")
    public void saveOverwritesExistingFileTest() {
        // Given
        Map<String, AccountHolder> firstMap = new HashMap<>();
        firstMap.put("oldUser", new AccountHolder("oldUser", "oldPass"));

        Map<String, AccountHolder> secondMap = new HashMap<>();
        secondMap.put("newUser", new AccountHolder("newUser", "newPass"));

        // When
        dataPersistenceService.save(firstMap);
        dataPersistenceService.save(secondMap);
        Map<String, AccountHolder> loadedMap = dataPersistenceService.load();

        // Then
        assertNotNull(loadedMap);
        assertEquals(1, loadedMap.size());
        assertTrue(loadedMap.containsKey("newUser"));
        assertFalse(loadedMap.containsKey("oldUser"));
    }

    @Test
    @DisplayName("Сохранение пользователя с полным набором данных в JSON")
    public void saveUserWithCompleteDataToJsonTest() {
        // Given
        AccountHolder accountHolder = new AccountHolder("completeUser", "password");
        accountHolder.getFinancialAccount().addTransaction(new FinancialEntry(150.0, "bonus", true));
        accountHolder.getFinancialAccount().addTransaction(new FinancialEntry(75.0, "entertainment", false));
        accountHolder.getFinancialAccount().setBudget("food", 500.0);
        accountHolder.getFinancialAccount().setBudget("transport", 200.0);

        // When
        dataPersistenceService.saveJSON(accountHolder, null);
        File jsonFile = new File("completeUser.json");

        // Then
        assertTrue(jsonFile.exists());

        // Cleanup
        jsonFile.delete();
    }

    @Test
    @DisplayName("Загрузка из несуществующего JSON файла должна возвращать null")
    public void loadNonExistentJsonFileTest() {
        // When
        AccountHolder result = dataPersistenceService.loadJSON(new File("nonexistent.json"));

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Загрузка поврежденного JSON файла должна возвращать null")
    public void loadCorruptedJsonFileTest() throws IOException {
        // Given
        File corruptedFile = new File("corrupted.json");
        Files.writeString(corruptedFile.toPath(), "invalid json content");

        // When
        AccountHolder result = dataPersistenceService.loadJSON(corruptedFile);

        // Then
        assertNull(result);

        // Cleanup
        corruptedFile.delete();
    }

    @Test
    @DisplayName("Сохранение и загрузка пользователя с бюджетом в JSON")
    public void saveAndLoadUserWithBudgetToJsonTest() {
        // Given
        AccountHolder originalAccountHolder = new AccountHolder("budgetUser", "pass");
        originalAccountHolder.getFinancialAccount().setBudget("food", 1000.0);
        originalAccountHolder.getFinancialAccount().setBudget("transport", 500.0);

        // When
        dataPersistenceService.saveJSON(originalAccountHolder, "budgetUser.json");
        AccountHolder loadedAccountHolder = dataPersistenceService.loadJSON(new File("budgetUser.json"));

        // Then
        assertNotNull(loadedAccountHolder);
        assertEquals("budgetUser", loadedAccountHolder.getUsername());
        assertEquals(1000.0, loadedAccountHolder.getFinancialAccount().getBudget("food"), 0);
        assertEquals(500.0, loadedAccountHolder.getFinancialAccount().getBudget("transport"), 0);

        // Cleanup
        new File("budgetUser.json").delete();
    }

    @Test
    @DisplayName("Сохранение пользователя без транзакций в JSON")
    public void saveUserWithoutTransactionsToJsonTest() {
        // Given
        AccountHolder accountHolder = new AccountHolder("emptyUser", "password");

        // When
        dataPersistenceService.saveJSON(accountHolder, "emptyUser.json");
        AccountHolder loadedAccountHolder = dataPersistenceService.loadJSON(new File("emptyUser.json"));

        // Then
        assertNotNull(loadedAccountHolder);
        assertEquals("emptyUser", loadedAccountHolder.getUsername());
        assertTrue(loadedAccountHolder.getFinancialAccount().getFinancialEntries().isEmpty());

        // Cleanup
        new File("emptyUser.json").delete();
    }

    @Test
    @DisplayName("Проверка целостности данных после сериализации/десериализации")
    public void dataIntegrityAfterSaveLoadTest() {
        // Given
        Map<String, AccountHolder> originalUsers = new HashMap<>();
        AccountHolder accountHolder = new AccountHolder("integrityUser", "password");
        accountHolder.getFinancialAccount().addTransaction(new FinancialEntry(300.0, "salary", true));
        accountHolder.getFinancialAccount().addTransaction(new FinancialEntry(150.0, "rent", false));
        accountHolder.getFinancialAccount().setBudget("entertainment", 200.0);
        originalUsers.put("integrityUser", accountHolder);

        // When
        dataPersistenceService.save(originalUsers);
        Map<String, AccountHolder> loadedUsers = dataPersistenceService.load();

        // Then
        assertNotNull(loadedUsers);
        AccountHolder loadedAccountHolder = loadedUsers.get("integrityUser");
        assertNotNull(loadedAccountHolder);
        assertEquals("integrityUser", loadedAccountHolder.getUsername());
        assertEquals(2, loadedAccountHolder.getFinancialAccount().getFinancialEntries().size());
        assertEquals(200.0, loadedAccountHolder.getFinancialAccount().getBudget("entertainment"), 0);
    }

    @Test
    @DisplayName("Сохранение пользователя с транзакциями, содержащими timestamp")
    public void saveUserWithTransactionsWithTimestampTest() {
        // Given
        AccountHolder accountHolder = new AccountHolder("timestampUser", "password");
        FinancialEntry financialEntry = new FinancialEntry(100.0, "test", true);

        accountHolder.getFinancialAccount().addTransaction(financialEntry);

        // When
        dataPersistenceService.saveJSON(accountHolder, "timestampUser.json");
        AccountHolder loadedAccountHolder = dataPersistenceService.loadJSON(new File("timestampUser.json"));

        // Then
        assertNotNull(loadedAccountHolder);
        assertEquals(1, loadedAccountHolder.getFinancialAccount().getFinancialEntries().size());
        assertNotNull(loadedAccountHolder.getFinancialAccount().getFinancialEntries().get(0).getTimestamp());

        // Cleanup
        new File("timestampUser.json").delete();
    }

    @Test
    @DisplayName("Резервное копирование данных")
    public void backupDataTest() {
        // Given
        Map<String, AccountHolder> users = new HashMap<>();
        users.put("user1", new AccountHolder("user1", "pass1"));

        // When
        dataPersistenceService.backupData(users);

        // Then - проверяем что файл создан (ищем файл с паттерном backup_users_*.data)
        File backupDir = new File(".");
        File[] backupFiles = backupDir.listFiles((dir, name) ->
                name.startsWith("backup_users_") && name.endsWith(".data"));

        assertNotNull(backupFiles);
        assertTrue(backupFiles.length > 0);
    }

    @Test
    @DisplayName("Сохранение JSON с кастомным именем файла")
    public void saveJsonWithCustomFilenameTest() {
        // Given
        AccountHolder accountHolder = new AccountHolder("customUser", "password");

        // When
        dataPersistenceService.saveJSON(accountHolder, "custom_backup.json");
        File jsonFile = new File("custom_backup.json");

        // Then
        assertTrue(jsonFile.exists());

        // Cleanup
        jsonFile.delete();
    }

    @Test
    @DisplayName("Загрузка JSON файла не в формате JSON")
    public void loadNonJsonFileTest() throws IOException {
        // Given
        File nonJsonFile = new File("not_json.txt");
        Files.writeString(nonJsonFile.toPath(), "This is not JSON");

        // When
        AccountHolder result = dataPersistenceService.loadJSON(nonJsonFile);

        // Then
        assertNull(result);

        // Cleanup
        nonJsonFile.delete();
    }
}