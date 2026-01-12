import com.core.exception.PasswordNotFoundException;
import com.core.exception.UserAlreadyCreatedException;
import com.core.exception.UserNotFoundException;
import com.core.model.User;
import com.core.service.LoginService;
import com.infra.MemoryUserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTest {

    private LoginService loginService;
    private User testUser;
    private User testUser2;

    @BeforeEach
    public void setUp() {
        loginService = new LoginService(new MemoryUserRepositoryImpl());
        testUser = new User("testUser", "testPassword");
        testUser2 = new User("anotherUser", "anotherPassword");
    }

    @Test
    @DisplayName("Регистрация нового пользователя")
    public void registrationNewUserTest() {
        // When
        User registeredUser = loginService.registration("newUser", "newPassword");

        // Then
        assertNotNull(registeredUser);
        assertEquals("newUser", registeredUser.getUsername());
        assertEquals("newPassword", registeredUser.getPassword());
        assertEquals(registeredUser, loginService.getCurrentUser());
    }

    @Test
    @DisplayName("Регистрация уже существующего пользователя должно бросать исключение")
    public void registrationExistingUserTest() {
        // Given
        loginService.registration("existingUser", "password");

        // When & Then
        assertThrows(UserAlreadyCreatedException.class, () -> {
            loginService.registration("existingUser", "differentPassword");
        });
    }

    @Test
    @DisplayName("Успешный логин существующего пользователя")
    public void loginSuccessTest() {
        // Given
        loginService.registration("validUser", "validPassword");
        loginService.unLogin();

        // When
        User loggedInUser = loginService.login("validUser", "validPassword");

        // Then
        assertNotNull(loggedInUser);
        assertEquals("validUser", loggedInUser.getUsername());
        assertEquals(loggedInUser, loginService.getCurrentUser());
    }

    @Test
    @DisplayName("Логин несуществующего пользователя должно бросать UserNotFoundException")
    public void loginNonExistentUserTest() {
        assertThrows(UserNotFoundException.class, () -> {
            loginService.login("nonExistent", "password");
        });
    }

    @Test
    @DisplayName("Логин с неправильным паролем должно бросать PasswordNotFoundException")
    public void loginWrongPasswordTest() {
        // Given
        loginService.registration("user", "correctPassword");
        loginService.unLogin();

        // When & Then
        assertThrows(PasswordNotFoundException.class, () -> {
            loginService.login("user", "wrongPassword");
        });
    }

    @Test
    @DisplayName("Проверка isLoggedIn когда пользователь залогинен")
    public void isLoggedInWhenUserLoggedInTest() {
        // Given
        loginService.registration("user", "password");

        // When & Then
        assertTrue(loginService.isLoggedIn());
    }

    @Test
    @DisplayName("Проверка isLoggedIn когда пользователь разлогинен")
    public void isLoggedInWhenUserLoggedOutTest() {
        // Given
        loginService.registration("user", "password");
        loginService.unLogin();

        // When & Then
        assertFalse(loginService.isLoggedIn());
    }

    @Test
    @DisplayName("Разлогинивание пользователя")
    public void unLoginTest() {
        // Given
        loginService.registration("user", "password");
        assertTrue(loginService.isLoggedIn());

        // When
        loginService.unLogin();

        // Then
        assertFalse(loginService.isLoggedIn());
        assertNull(loginService.getCurrentUser());
    }

    @Test
    @DisplayName("handleLogin для нового пользователя (регистрация)")
    public void handleLoginNewUserTest() {
        // When
        User result = loginService.handleLogin("newUser", "newPassword");

        // Then
        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
        assertTrue(loginService.isLoggedIn());
    }

    @Test
    @DisplayName("handleLogin для существующего пользователя (логин)")
    public void handleLoginExistingUserTest() {
        // Given
        loginService.registration("existingUser", "password");
        loginService.unLogin();

        // When
        User result = loginService.handleLogin("existingUser", "password");

        // Then
        assertNotNull(result);
        assertEquals("existingUser", result.getUsername());
        assertTrue(loginService.isLoggedIn());
    }

    @Test
    @DisplayName("Установка и получение карты пользователей")
    public void setAndGetUserMapTest() {
        // Given
        Map<String, User> userMap = new HashMap<>();
        userMap.put("user1", new User("user1", "pass1"));
        userMap.put("user2", new User("user2", "pass2"));

        // When
        loginService.setUserMap(userMap);
        Map<String, User> retrievedMap = loginService.getUserMap();

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
        Map<String, User> userMap = loginService.getUserMap();

        // Then
        assertNotNull(userMap);
        assertTrue(userMap.isEmpty());
    }

    @Test
    @DisplayName("handleLogin с неправильным паролем для существующего пользователя")
    public void handleLoginWrongPasswordTest() {
        // Given
        loginService.registration("user", "correctPassword");
        loginService.unLogin();

        // When & Then
        assertThrows(PasswordNotFoundException.class, () -> {
            loginService.handleLogin("user", "wrongPassword");
        });
    }

    @Test
    @DisplayName("Последовательная регистрация нескольких пользователей")
    public void sequentialRegistrationTest() {
        // When
        User user1 = loginService.registration("user1", "pass1");
        loginService.unLogin();
        User user2 = loginService.registration("user2", "pass2");

        // Then
        assertEquals("user2", loginService.getCurrentUser().getUsername());

        Map<String, User> userMap = loginService.getUserMap();
        assertEquals(2, userMap.size());
        assertTrue(userMap.containsKey("user1"));
        assertTrue(userMap.containsKey("user2"));
    }

    @Test
    @DisplayName("Проверка что текущий пользователь обновляется при логине")
    public void currentUserUpdatedOnLoginTest() {
        // Given
        loginService.registration("user1", "pass1");
        User firstUser = loginService.getCurrentUser();
        loginService.unLogin();
        loginService.registration("user2", "pass2");
        User secondUser = loginService.getCurrentUser();

        // Then
        assertNotEquals(firstUser, secondUser);
        assertEquals("user2", secondUser.getUsername());
    }

    @Test
    @DisplayName("Повторный логин после разлогинивания")
    public void reloginAfterUnloginTest() {
        // Given
        User originalUser = loginService.registration("user", "password");
        loginService.unLogin();

        // When
        User reloggedUser = loginService.login("user", "password");

        // Then
        assertEquals(originalUser, reloggedUser);
        assertEquals(reloggedUser, loginService.getCurrentUser());
    }

    @Test
    @DisplayName("Регистрация пользователя с пустым именем")
    public void registrationEmptyUsernameTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            loginService.registration("", "password");
        });
    }

}