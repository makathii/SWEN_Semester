package at.technikum_wien.services;

import at.technikum_wien.database.repositories.TokenRepository;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.models.entities.User;
import at.technikum_wien.security.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private UserService userService;

    // ========== REGISTER USER TESTS ==========

    @Test
    void registerUser_NewUsername_SavesUserWithHashedPassword() {
        // Arrange
        when(userRepository.getByName("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(100); // Simulate DB assigning ID
            return savedUser;
        });

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword("mypassword123"))
                    .thenReturn("HASHED_mypassword123");

            // Act
            User result = userService.registerUser("newuser", "mypassword123");

            // Assert
            assertEquals(100, result.getId());
            assertEquals("newuser", result.getUsername());
            assertEquals("HASHED_mypassword123", result.getPasswordHash());

            verify(userRepository).getByName("newuser");
            verify(userRepository).save(argThat(user ->
                    user.getUsername().equals("newuser") &&
                            user.getPasswordHash().equals("HASHED_mypassword123")
            ));

            mockedHasher.verify(() -> PasswordHasher.hashPassword("mypassword123"));
        }
    }

    @Test
    void registerUser_ExistingUsername_ThrowsIllegalArgumentException() {
        // Arrange
        User existingUser = new User("existing", "hash");
        existingUser.setId(1);
        when(userRepository.getByName("existing")).thenReturn(existingUser);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser("existing", "password")
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_PasswordHashingFails_PropagatesException() {
        // Arrange
        when(userRepository.getByName("user")).thenReturn(null);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword("password"))
                    .thenThrow(new RuntimeException("Hashing failed"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userService.registerUser("user", "password")
            );

            assertEquals("Hashing failed", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    // ========== LOGIN USER TESTS ==========

    @Test
    void loginUser_ValidCredentials_ReturnsTokenAndUserId() {
        // Arrange
        User storedUser = new User("alice", "HASHED_PW");
        storedUser.setId(42);

        when(userRepository.getByName("alice")).thenReturn(storedUser);
        when(tokenRepository.createToken(42, "alice")).thenReturn("alice-mrpToken");

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.checkPassword("secret123", "HASHED_PW"))
                    .thenReturn(true);

            // Act
            UserService.LoginResult result = userService.loginUser("alice", "secret123");

            // Assert
            assertEquals(42, result.userId());
            assertEquals("alice-mrpToken", result.token());

            verify(tokenRepository).createToken(42, "alice");
            mockedHasher.verify(() -> PasswordHasher.checkPassword("secret123", "HASHED_PW"));
        }
    }

    @Test
    void loginUser_NonExistentUser_ThrowsSecurityException() {
        // Arrange
        when(userRepository.getByName("ghost")).thenReturn(null);

        // Act & Assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> userService.loginUser("ghost", "password")
        );

        assertEquals("Invalid username or password", exception.getMessage());
        verify(tokenRepository, never()).createToken(anyInt(), anyString());
    }

    @Test
    void loginUser_WrongPassword_ThrowsSecurityException() {
        // Arrange
        User storedUser = new User("bob", "CORRECT_HASH");
        storedUser.setId(99);

        when(userRepository.getByName("bob")).thenReturn(storedUser);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.checkPassword("wrong", "CORRECT_HASH"))
                    .thenReturn(false);

            // Act & Assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userService.loginUser("bob", "wrong")
            );

            assertEquals("Invalid username or password", exception.getMessage());
            verify(tokenRepository, never()).createToken(anyInt(), anyString());
            mockedHasher.verify(() -> PasswordHasher.checkPassword("wrong", "CORRECT_HASH"));
        }
    }

    @Test
    void loginResult_ConstructorAndFields() {
        // Test the nested LoginResult class
        UserService.LoginResult result = new UserService.LoginResult("token123", 456);

        assertEquals("token123", result.token());
        assertEquals(456, result.userId());
    }

    // ========== UPDATE USER TESTS ==========

    @Test
    void updateUser_ValidUpdate_ReturnsUpdatedUser() {
        // Arrange
        User existingUser = new User("oldname", "oldhash");
        existingUser.setId(1);
        existingUser.setFavoriteGenre("Action");

        when(userRepository.getById(1)).thenReturn(existingUser);
        when(userRepository.getByName("newname")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        User updateData = new User("newname", "newhash");
        updateData.setId(1);
        updateData.setFavoriteGenre("Comedy");

        // Act
        User result = userService.updateUser(updateData);

        // Assert
        assertEquals("newname", result.getUsername());
        assertEquals("newhash", result.getPasswordHash());
        assertEquals("Comedy", result.getFavoriteGenre());

        verify(userRepository).getById(1);
        verify(userRepository).getByName("newname");
        verify(userRepository).save(updateData);
    }

    @Test
    void updateUser_UsernameTakenByDifferentUser_ThrowsException() {
        // Arrange
        User existingUser = new User("user1", "hash1");
        existingUser.setId(1);

        User otherUser = new User("taken", "hash2");
        otherUser.setId(2); // Different ID!

        when(userRepository.getById(1)).thenReturn(existingUser);
        when(userRepository.getByName("taken")).thenReturn(otherUser);

        User updateData = new User("taken", "newhash");
        updateData.setId(1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(updateData)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_SameUsername_DifferentUser_ThrowsException() {
        // Test: User 1 tries to take username of User 2
        // Arrange
        User user1 = new User("alice", "hash1");
        user1.setId(1);

        User user2 = new User("bob", "hash2");
        user2.setId(2);

        when(userRepository.getById(1)).thenReturn(user1);
        when(userRepository.getByName("bob")).thenReturn(user2); // Returns user2

        User updateData = new User("bob", "newhash");
        updateData.setId(1); // user1 trying to become "bob"

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(updateData)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_SameUsername_SameUser_UpdatesSuccessfully() {
        // Arrange - User keeps same username but changes other fields
        User existingUser = new User("same", "oldhash");
        existingUser.setId(5);
        existingUser.setFavoriteGenre("Drama");

        when(userRepository.getById(5)).thenReturn(existingUser);
        when(userRepository.getByName("same")).thenReturn(existingUser); // Returns self
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        User updateData = new User("same", "newhash"); // Same username
        updateData.setId(5);
        updateData.setFavoriteGenre("Comedy");

        // Act
        User result = userService.updateUser(updateData);

        // Assert - Should allow same username
        assertEquals("same", result.getUsername());
        assertEquals("newhash", result.getPasswordHash());
        assertEquals("Comedy", result.getFavoriteGenre());
        verify(userRepository).save(updateData);
    }

    @Test
    void updateUser_KeepExistingFavoriteGenre_WhenNullInUpdate() {
        // Arrange
        User existingUser = new User("user", "hash");
        existingUser.setId(10);
        existingUser.setFavoriteGenre("Sci-Fi"); // Has existing favorite

        when(userRepository.getById(10)).thenReturn(existingUser);
        when(userRepository.getByName("user")).thenReturn(existingUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        User updateData = new User("user", "newhash");
        updateData.setId(10);
        updateData.setFavoriteGenre(null); // Not setting favorite genre

        // Act
        User result = userService.updateUser(updateData);

        // Assert - Should keep existing favorite genre
        assertEquals("Sci-Fi", result.getFavoriteGenre());
        assertEquals("newhash", result.getPasswordHash());
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.getById(999)).thenReturn(null);

        User updateData = new User("new", "hash");
        updateData.setId(999);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(updateData)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ========== GET USER BY ID TESTS ==========

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        // Arrange
        User expectedUser = new User("test", "hash");
        expectedUser.setId(50);

        when(userRepository.getById(50)).thenReturn(expectedUser);

        // Act
        User result = userService.getUserById(50);

        // Assert
        assertSame(expectedUser, result);
        verify(userRepository).getById(50);
    }

    @Test
    void getUserById_NonExistentUser_ReturnsNull() {
        // Arrange
        when(userRepository.getById(999)).thenReturn(null);

        // Act
        User result = userService.getUserById(999);

        // Assert
        assertNull(result);
        verify(userRepository).getById(999);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void registerUser_NullUsername_ThrowsException() {
        // Act & Assert - NullPointerException from PasswordHasher or User constructor
        assertThrows(Exception.class, () ->
                userService.registerUser(null, "password")
        );
    }

    @Test
    void registerUser_NullPassword_ThrowsException() {
        // Arrange
        when(userRepository.getByName("user")).thenReturn(null);

        // Act & Assert - NullPointerException from PasswordHasher
        assertThrows(NullPointerException.class, () ->
                userService.registerUser("user", null)
        );
    }

    @Test
    void loginUser_NullUsername_ThrowsException() {
        // Act & Assert - Might throw NPE or SecurityException
        assertThrows(Exception.class, () ->
                userService.loginUser(null, "password")
        );
    }

    @Test
    void loginUser_NullPassword_ThrowsException() {
        // Arrange
        User user = new User("user", "hash");
        when(userRepository.getByName("user")).thenReturn(user);

        // Act & Assert - NullPointerException from PasswordHasher
        assertThrows(NullPointerException.class, () ->
                userService.loginUser("user", null)
        );
    }

    @Test
    void updateUser_NullUser_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                userService.updateUser(null)
        );
    }
}