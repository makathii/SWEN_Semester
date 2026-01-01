package at.technikum_wien.services;

import at.technikum_wien.database.repositories.TokenRepository;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.models.entities.User;
import at.technikum_wien.security.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    TokenRepository tokenRepository;

    @InjectMocks
    UserService userService;

    //gut
    @Test
    void registerUser_usernameExists() {
        when(userRepository.getByName("bob")).thenReturn(new User());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.registerUser("bob", "pw"));
        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void loginUser_success_and_token() {
        User stored = new User("carl", "HASHEDPW");
        stored.setId(55);
        when(userRepository.getByName("carl")).thenReturn(stored);
        when(tokenRepository.createToken(55, "carl")).thenReturn("tok-55");

        try (MockedStatic<PasswordHasher> mocked = mockStatic(PasswordHasher.class)) {
            mocked.when(() -> PasswordHasher.checkPassword("pw", "HASHEDPW")).thenReturn(true);

            UserService.LoginResult res = userService.loginUser("carl", "pw");
            assertEquals(55, res.userId);
            assertEquals("tok-55", res.token);

            mocked.verify(() -> PasswordHasher.checkPassword("pw", "HASHEDPW"));
        }
    }

    @Test
    void loginUser_invalidUsername() {
        when(userRepository.getByName("doesnot")).thenReturn(null);
        SecurityException ex = assertThrows(SecurityException.class, () -> userService.loginUser("doesnot", "pw"));
        assertTrue(ex.getMessage().contains("Invalid username or password"));
    }

    @Test
    void loginUser_wrongPassword() {
        User stored = new User("d", "HASHED");
        stored.setId(66);
        when(userRepository.getByName("d")).thenReturn(stored);

        try (MockedStatic<PasswordHasher> mocked = mockStatic(PasswordHasher.class)) {
            mocked.when(() -> PasswordHasher.checkPassword("bad", "HASHED")).thenReturn(false);
            SecurityException ex = assertThrows(SecurityException.class, () -> userService.loginUser("d", "bad"));
            assertTrue(ex.getMessage().contains("Invalid username or password"));
            mocked.verify(() -> PasswordHasher.checkPassword("bad", "HASHED"));
        }
    }

    @Test
    void updateUser_success_and_usernameChangeConflict() {
        User existing = new User("old", "h");
        existing.setId(10);
        when(userRepository.getById(10)).thenReturn(existing);

        User other = new User("taken", "h2");
        other.setId(99);
        when(userRepository.getByName("taken")).thenReturn(other);

        User toUpdate = new User("taken", "h2");
        toUpdate.setId(10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(toUpdate));
        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void updateUser_success_when_available() {
        User existing = new User("old", "h");
        existing.setId(20);
        when(userRepository.getById(20)).thenReturn(existing);
        when(userRepository.getByName("new")).thenReturn(null);

        User toUpdate = new User("new", "h");
        toUpdate.setId(20);
        when(userRepository.save(toUpdate)).thenReturn(toUpdate);

        User out = userService.updateUser(toUpdate);
        assertEquals("new", out.getUsername());
    }

    @Test
    void getUserById_delegates() {
        User u = new User();
        u.setId(500);
        when(userRepository.getById(500)).thenReturn(u);
        assertSame(u, userService.getUserById(500));
    }
}
