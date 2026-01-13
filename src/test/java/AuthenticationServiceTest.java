import com.business.exception.PasswordMismatchException;
import com.business.exception.UserAlreadyExistsException;
import com.business.exception.UserMissingException;
import com.business.entities.AccountHolder;
import com.business.services.AuthenticationService;
import com.infrastructure.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationServiceTest {

    private AuthenticationService authenticationService;
    private AccountHolder testAccountHolder;

    @BeforeEach
    public void setUp() {
        authenticationService = new AuthenticationService(new InMemoryUserRepository());
        testAccountHolder = new AccountHolder("testUser", "testPassword");
    }

    @Test
    @DisplayName("Регистрация нового пользователя")
    public void registrationNewUserTest() {
        // When
        AccountHolder registeredAccountHolder = authenticationService.registration("newUser", "newPassword");

        // Then
        assertNotNull(registeredAccountHolder);
        assertEquals("newUser", registeredAccountHolder.getUsername());
        assertEquals("newPassword", registeredAccountHolder.getPassword());
        assertEquals(registeredAccountHolder, authenticationService.getCurrentAccountHolder());
    }

    @Test
    @DisplayName("Регистрация уже существующего пользователя должно бросать исключение")
    public void registrationExistingUserTest() {
        // Given
        authenticationService.registration("existingUser", "password");

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> {
            authenticationService.registration("existingUser", "differentPassword");
        });
    }

    @Test
    @DisplayName("Успешный логин существующего пользователя")
    public void loginSuccessTest() {
        // Given
        authenticationService.registration("validUser", "validPassword");
        authenticationService.unLogin();

        // When
        AccountHolder loggedInAccountHolder = authenticationService.login("validUser", "validPassword");

        // Then
        assertNotNull(loggedInAccountHolder);
        assertEquals("validUser", loggedInAccountHolder.getUsername());
        assertEquals(loggedInAccountHolder, authenticationService.getCurrentAccountHolder());
    }

    @Test
    @DisplayName("Логин несуществующего пользователя должно бросать UserNotFoundException")
    public void loginNonExistentUserTest() {
        assertThrows(UserMissingException.class, () -> {
            authenticationService.login("nonExistent", "password");
        });
    }

    @Test
    @DisplayName("Логин с неправильным паролем должно бросать PasswordMismatchException")
    public void loginWrongPasswordTest() {
        // Given
        authenticationService.registration("user", "correctPassword");
        authenticationService.unLogin();

        // When & Then
        assertThrows(PasswordMismatchException.class, () -> {
            authenticationService.login("user", "wrongPassword");
        });
    }

    @Test
    @DisplayName("Проверка isLoggedIn когда пользователь залогинен")
    public void isLoggedInWhenUserLoggedInTest() {
        // Given
        authenticationService.registration("user", "password");

        // When & Then
        assertTrue(authenticationService.isLoggedIn());
    }

    @Test
    @DisplayName("Проверка isLoggedIn когда пользователь разлогинен")
    public void isLoggedInWhenUserLoggedOutTest() {
        // Given
        authenticationService.registration("user", "password");
        authenticationService.unLogin();

        // When & Then
        assertFalse(authenticationService.isLoggedIn());
    }

    @Test
    @DisplayName("Разлогинивание пользователя")
    public void unLoginTest() {
        // Given
        authenticationService.registration("user", "password");
        assertTrue(authenticationService.isLoggedIn());

        // When
        authenticationService.unLogin();

        // Then
        assertFalse(authenticationService.isLoggedIn());
        assertNull(authenticationService.getCurrentAccountHolder());
    }

    @Test
    @DisplayName("handleLogin для нового пользователя (регистрация)")
    public void handleLoginNewUserTest() {
        // When
        AccountHolder result = authenticationService.handleLogin("newUser", "newPassword");

        // Then
        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
        assertTrue(authenticationService.isLoggedIn());
    }

    @Test
    @DisplayName("handleLogin для существующего пользователя (логин)")
    public void handleLoginExistingUserTest() {
        // Given
        authenticationService.registration("existingUser", "password");
        authenticationService.unLogin();

        // When
        AccountHolder result = authenticationService.handleLogin("existingUser", "password");

        // Then
        assertNotNull(result);
        assertEquals("existingUser", result.getUsername());
        assertTrue(authenticationService.isLoggedIn());
    }

    @Test
    @DisplayName("Установка и получение карты пользователей")
    public void setAndGetUserMapTest() {
        // Given
        Map<String, AccountHolder> userMap = new HashMap<>();
        userMap.put("user1", new AccountHolder("user1", "pass1"));
        userMap.put("user2", new AccountHolder("user2", "pass2"));

        // When
        authenticationService.setUserMap(userMap);
        Map<String, AccountHolder> retrievedMap = authenticationService.getUserMap();

        // Then
        assertNotNull(retrievedMap);
        assertEquals(2, retrievedMap.size());
        assertTrue(retrievedMap.containsKey("user1"));
        assertTrue(retrievedMap.containsKey("user2"));
    }

    @Test
    @DisplayName("Получение пустой карты пользователей")
    public void getEmptyUserMapTest() {
        // When
        Map<String, AccountHolder> userMap = authenticationService.getUserMap();

        // Then
        assertNotNull(userMap);
        assertTrue(userMap.isEmpty());
    }

    @Test
    @DisplayName("handleLogin с неправильным паролем для существующего пользователя")
    public void handleLoginWrongPasswordTest() {
        // Given
        authenticationService.registration("user", "correctPassword");
        authenticationService.unLogin();

        // When & Then
        assertThrows(PasswordMismatchException.class, () -> {
            authenticationService.handleLogin("user", "wrongPassword");
        });
    }

    @Test
    @DisplayName("Последовательная регистрация нескольких пользователей")
    public void sequentialRegistrationTest() {
        // When
        AccountHolder accountHolder1 = authenticationService.registration("user1", "pass1");
        authenticationService.unLogin();
        AccountHolder accountHolder2 = authenticationService.registration("user2", "pass2");

        // Then
        assertEquals("user2", authenticationService.getCurrentAccountHolder().getUsername());

        Map<String, AccountHolder> userMap = authenticationService.getUserMap();
        assertEquals(2, userMap.size());
        assertTrue(userMap.containsKey("user1"));
        assertTrue(userMap.containsKey("user2"));
    }

    @Test
    @DisplayName("Проверка что текущий пользователь обновляется при логине")
    public void currentUserUpdatedOnLoginTest() {
        // Given
        authenticationService.registration("user1", "pass1");
        AccountHolder firstAccountHolder = authenticationService.getCurrentAccountHolder();
        authenticationService.unLogin();
        authenticationService.registration("user2", "pass2");
        AccountHolder secondAccountHolder = authenticationService.getCurrentAccountHolder();

        // Then
        assertNotEquals(firstAccountHolder, secondAccountHolder);
        assertEquals("user2", secondAccountHolder.getUsername());
    }

    @Test
    @DisplayName("Повторный логин после разлогинивания")
    public void reloginAfterUnloginTest() {
        // Given
        AccountHolder originalAccountHolder = authenticationService.registration("user", "password");
        authenticationService.unLogin();

        // When
        AccountHolder reloggedAccountHolder = authenticationService.login("user", "password");

        // Then
        assertEquals(originalAccountHolder, reloggedAccountHolder);
        assertEquals(reloggedAccountHolder, authenticationService.getCurrentAccountHolder());
    }

    @Test
    @DisplayName("Регистрация пользователя с пустым именем")
    public void registrationEmptyUsernameTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.registration("", "password");
        });
    }

    @Test
    @DisplayName("Регистрация пользователя с коротким именем (<3 символов)")
    public void registrationShortUsernameTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.registration("ab", "password");
        });
    }

    @Test
    @DisplayName("Регистрация пользователя с коротким паролем (<4 символов)")
    public void registrationShortPasswordTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.registration("user", "123");
        });
    }

    @Test
    @DisplayName("Логин с пустыми данными")
    public void loginWithEmptyCredentialsTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.login("", "");
        });
    }
}