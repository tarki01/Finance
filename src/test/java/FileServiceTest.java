import com.core.model.Transaction;
import com.core.model.User;
import com.core.service.FileService;
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

public class FileServiceTest {

    private FileService fileService;
    private User testUser;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        fileService = new FileService();
        testUser = new User("testUser", "password123");
        testUser.getWallet().addTransaction(new Transaction(100.0, "salary", true));
        testUser.getWallet().addTransaction(new Transaction(50.0, "food", false));
        testUser.getWallet().setBudget("food", 300.0);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Очистка созданных файлов после каждого теста
        Files.deleteIfExists(Path.of("users.data"));
        Files.deleteIfExists(Path.of("testUser.json"));
        Files.deleteIfExists(Path.of("nonexistent.json"));
    }

    @Test
    @DisplayName("Сохранение и загрузка пустой Map пользователей")
    public void saveAndLoadEmptyMapTest() {
        // Given
        Map<String, User> emptyMap = new HashMap<>();

        // When
        fileService.save(emptyMap);
        Map<String, User> loadedMap = fileService.load();

        // Then
        assertNotNull(loadedMap);
        assertTrue(loadedMap.isEmpty());
    }

    @Test
    @DisplayName("Сохранение и загрузка Map с несколькими пользователями")
    public void saveAndLoadMultipleUsersTest() {
        // Given
        Map<String, User> users = new HashMap<>();
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        user1.getWallet().addTransaction(new Transaction(100.0, "income", true));
        user2.getWallet().addTransaction(new Transaction(200.0, "expense", false));

        users.put("user1", user1);
        users.put("user2", user2);

        // When
        fileService.save(users);
        Map<String, User> loadedUsers = fileService.load();

        // Then
        assertNotNull(loadedUsers);
        assertEquals(2, loadedUsers.size());
        assertTrue(loadedUsers.containsKey("user1"));
        assertTrue(loadedUsers.containsKey("user2"));
        assertEquals(100.0, loadedUsers.get("user1").getWallet().getTransactions().get(0).getAmount(), 0);
        assertEquals(200.0, loadedUsers.get("user2").getWallet().getTransactions().get(0).getAmount(), 0);
    }

    @Test
    @DisplayName("Загрузка из несуществующего файла должна возвращать null")
    public void loadNonExistentFileTest() {
        // When
        Map<String, User> result = fileService.load();

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Перезапись существующего файла при сохранении")
    public void saveOverwritesExistingFileTest() {
        // Given
        Map<String, User> firstMap = new HashMap<>();
        firstMap.put("oldUser", new User("oldUser", "oldPass"));

        Map<String, User> secondMap = new HashMap<>();
        secondMap.put("newUser", new User("newUser", "newPass"));

        // When
        fileService.save(firstMap);
        fileService.save(secondMap);
        Map<String, User> loadedMap = fileService.load();

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
        User user = new User("completeUser", "password");
        user.getWallet().addTransaction(new Transaction(150.0, "bonus", true));
        user.getWallet().addTransaction(new Transaction(75.0, "entertainment", false));
        user.getWallet().setBudget("food", 500.0);
        user.getWallet().setBudget("transport", 200.0);

        // When
        fileService.saveJSON(user);
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
        User result = fileService.loadJSON(new File("nonexistent.json"));

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
        User result = fileService.loadJSON(corruptedFile);

        // Then
        assertNull(result);

        // Cleanup
        corruptedFile.delete();
    }

    @Test
    @DisplayName("Сохранение и загрузка пользователя с бюджетом в JSON")
    public void saveAndLoadUserWithBudgetToJsonTest() {
        // Given
        User originalUser = new User("budgetUser", "pass");
        originalUser.getWallet().setBudget("food", 1000.0);
        originalUser.getWallet().setBudget("transport", 500.0);

        // When
        fileService.saveJSON(originalUser);
        User loadedUser = fileService.loadJSON(new File("budgetUser.json"));

        // Then
        assertNotNull(loadedUser);
        assertEquals("budgetUser", loadedUser.getUsername());
        assertEquals(1000.0, loadedUser.getWallet().getBudget("food"), 0);
        assertEquals(500.0, loadedUser.getWallet().getBudget("transport"), 0);

        // Cleanup
        new File("budgetUser.json").delete();
    }

    @Test
    @DisplayName("Сохранение пользователя без транзакций в JSON")
    public void saveUserWithoutTransactionsToJsonTest() {
        // Given
        User user = new User("emptyUser", "password");

        // When
        fileService.saveJSON(user);
        User loadedUser = fileService.loadJSON(new File("emptyUser.json"));

        // Then
        assertNotNull(loadedUser);
        assertEquals("emptyUser", loadedUser.getUsername());
        assertTrue(loadedUser.getWallet().getTransactions().isEmpty());

        // Cleanup
        new File("emptyUser.json").delete();
    }

    @Test
    @DisplayName("Проверка целостности данных после сериализации/десериализации")
    public void dataIntegrityAfterSaveLoadTest() {
        // Given
        Map<String, User> originalUsers = new HashMap<>();
        User user = new User("integrityUser", "password");
        user.getWallet().addTransaction(new Transaction(300.0, "salary", true));
        user.getWallet().addTransaction(new Transaction(150.0, "rent", false));
        user.getWallet().setBudget("entertainment", 200.0);
        originalUsers.put("integrityUser", user);

        // When
        fileService.save(originalUsers);
        Map<String, User> loadedUsers = fileService.load();

        // Then
        assertNotNull(loadedUsers);
        User loadedUser = loadedUsers.get("integrityUser");
        assertNotNull(loadedUser);
        assertEquals("integrityUser", loadedUser.getUsername());
        assertEquals(2, loadedUser.getWallet().getTransactions().size());
        assertEquals(200.0, loadedUser.getWallet().getBudget("entertainment"), 0);
    }

    @Test
    @DisplayName("Сохранение пользователя с транзакциями, содержащими timestamp")
    public void saveUserWithTransactionsWithTimestampTest() {
        // Given
        User user = new User("timestampUser", "password");
        Transaction transaction = new Transaction(100.0, "test", true);
        // Transaction автоматически устанавливает timestamp при создании

        user.getWallet().addTransaction(transaction);

        // When
        fileService.saveJSON(user);
        User loadedUser = fileService.loadJSON(new File("timestampUser.json"));

        // Then
        assertNotNull(loadedUser);
        assertEquals(1, loadedUser.getWallet().getTransactions().size());
        assertNotNull(loadedUser.getWallet().getTransactions().get(0).getTimestamp());

        // Cleanup
        new File("timestampUser.json").delete();
    }
}