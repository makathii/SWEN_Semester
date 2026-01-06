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

    //REGISTER TESTS
    @Test
    void registerUser_NewUsername_SavesUserWithHashedPassword() {
        //arrange
        when(userRepository.getByName("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(100);
            return savedUser;
        });

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword("password123"))
                    .thenReturn("HASHED_password123");

            //act
            User result = userService.registerUser("newuser", "password123");

            //assert
            assertEquals(100, result.getId());
            assertEquals("newuser", result.getUsername());
            assertEquals("HASHED_password123", result.getPasswordHash());

            verify(userRepository).getByName("newuser");
            verify(userRepository).save(argThat(user ->
                    user.getUsername().equals("newuser") &&
                            user.getPasswordHash().equals("HASHED_password123")
            ));

            mockedHasher.verify(() -> PasswordHasher.hashPassword("password123"));
        }
    }

    @Test
    void registerUser_ExistingUsername_ThrowsIllegalArgumentException() {
        //arrange
        User existingUser = new User("existing", "hash");
        existingUser.setId(1);
        when(userRepository.getByName("existing")).thenReturn(existingUser);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser("existing", "password")
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_PasswordHashingFails_PropagatesException() {
        //arrange
        when(userRepository.getByName("user")).thenReturn(null);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword("password123"))
                    .thenThrow(new RuntimeException("Hashing failed"));

            //act & <ssert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userService.registerUser("user", "password123")
            );

            assertEquals("Hashing failed", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    //LOGIN TESTS
    @Test
    void loginUser_ValidCredentials_ReturnsTokenAndUserId() {
        //arrange
        User storedUser = new User("capri", "HASHED_PW");
        storedUser.setId(42);

        when(userRepository.getByName("capri")).thenReturn(storedUser);
        when(tokenRepository.createToken(42, "capri")).thenReturn("capri-mrpToken");

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.checkPassword("password123", "HASHED_PW"))
                    .thenReturn(true);

            //act
            UserService.LoginResult result = userService.loginUser("capri", "password123");

            //assert
            assertEquals(42, result.userId());
            assertEquals("capri-mrpToken", result.token());

            verify(tokenRepository).createToken(42, "capri");
            mockedHasher.verify(() -> PasswordHasher.checkPassword("password123", "HASHED_PW"));
        }
    }

    @Test
    void loginUser_NonExistentUser_ThrowsSecurityException() {
        //arrange
        when(userRepository.getByName("vali")).thenReturn(null);

        //act & assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> userService.loginUser("vali", "password123")
        );

        assertEquals("Invalid username or password", exception.getMessage());
        verify(tokenRepository, never()).createToken(anyInt(), anyString());
    }

    @Test
    void loginUser_WrongPassword_ThrowsSecurityException() {
        //arrange
        User storedUser = new User("toffee", "CORRECT_HASH");
        storedUser.setId(99);

        when(userRepository.getByName("toffee")).thenReturn(storedUser);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.checkPassword("wrong", "CORRECT_HASH"))
                    .thenReturn(false);

            //act & assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userService.loginUser("toffee", "wrong")
            );

            assertEquals("Invalid username or password", exception.getMessage());
            verify(tokenRepository, never()).createToken(anyInt(), anyString());
            mockedHasher.verify(() -> PasswordHasher.checkPassword("wrong", "CORRECT_HASH"));
        }
    }

    @Test
    void loginResult_ConstructorAndFields() {
        //test nested LoginResult class
        UserService.LoginResult result = new UserService.LoginResult("token123", 456);

        assertEquals("token123", result.token());
        assertEquals(456, result.userId());
    }

    //UPDATE TESTS
    @Test
    void updateUser_ValidUpdate_ReturnsUpdatedUser() {
        //arrange
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

        //act
        User result = userService.updateUser(updateData);

        //assert
        assertEquals("newname", result.getUsername());
        assertEquals("newhash", result.getPasswordHash());
        assertEquals("Comedy", result.getFavoriteGenre());

        verify(userRepository).getById(1);
        verify(userRepository).getByName("newname");
        verify(userRepository).save(updateData);
    }

    @Test
    void updateUser_UsernameTakenByDifferentUser_ThrowsException() {
        //arrange
        User existingUser = new User("user1", "hash1");
        existingUser.setId(1);

        User otherUser = new User("taken", "hash2");
        otherUser.setId(2); //different ID!

        when(userRepository.getById(1)).thenReturn(existingUser);
        when(userRepository.getByName("taken")).thenReturn(otherUser);

        User updateData = new User("taken", "newhash");
        updateData.setId(1);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(updateData)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_SameUsername_DifferentUser_ThrowsException() {
        //User 1 tries to take username of User 2
        //arrange
        User user1 = new User("thief", "hash1");
        user1.setId(1);

        User user2 = new User("bob", "hash2");
        user2.setId(2);

        when(userRepository.getById(1)).thenReturn(user1);
        when(userRepository.getByName("bob")).thenReturn(user2); //returns user2

        User updateData = new User("bob", "newhash");
        updateData.setId(1); //user1 trying to become "bob"

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(updateData)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_SameUsername_SameUser_UpdatesSuccessfully() {
        //arrange - User keeps same username but changes other fields
        User existingUser = new User("same", "oldhash");
        existingUser.setId(5);
        existingUser.setFavoriteGenre("Drama");

        when(userRepository.getById(5)).thenReturn(existingUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        User updateData = new User("same", "newhash"); //same username
        updateData.setId(5);
        updateData.setFavoriteGenre("Comedy");

        //act
        User result = userService.updateUser(updateData);

        //assert - should allow same username
        assertEquals("same", result.getUsername());
        assertEquals("newhash", result.getPasswordHash());
        assertEquals("Comedy", result.getFavoriteGenre());
        verify(userRepository).save(updateData);
    }

    @Test
    void updateUser_KeepExistingFavoriteGenre_WhenNullInUpdate() {
        //arrange
        User existingUser = new User("user", "hash");
        existingUser.setId(10);
        existingUser.setFavoriteGenre("Sci-Fi"); //has existing favorite

        when(userRepository.getById(10)).thenReturn(existingUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        User updateData = new User("user", "newhash");
        updateData.setId(10);
        updateData.setFavoriteGenre(null); //not setting favorite genre

        //act
        User result = userService.updateUser(updateData);

        //assert - should keep existing favorite genre
        assertEquals("Sci-Fi", result.getFavoriteGenre());
        assertEquals("newhash", result.getPasswordHash());
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        //arrange
        when(userRepository.getById(999)).thenReturn(null);

        User updateData = new User("new", "hash");
        updateData.setId(999);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(updateData)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    //GET BY ID TESTS
    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        //arrange
        User expectedUser = new User("test", "hash");
        expectedUser.setId(50);

        when(userRepository.getById(50)).thenReturn(expectedUser);

        //act
        User result = userService.getUserById(50);

        //assert
        assertSame(expectedUser, result);
        verify(userRepository).getById(50);
    }

    @Test
    void getUserById_NonExistentUser_ReturnsNull() {
        //arrange
        when(userRepository.getById(999)).thenReturn(null);

        //act
        User result = userService.getUserById(999);

        //assert
        assertNull(result);
        verify(userRepository).getById(999);
    }

    //EDGE CASE TESTS
    @Test
    void registerUser_NullPassword_ThrowsException() {
        //arrange
        when(userRepository.getByName("user")).thenReturn(null);

        //act & assert - NullPointerException from PasswordHasher
        assertThrows(NullPointerException.class, () ->
                userService.registerUser("user", null)
        );
    }

    @Test
    void loginUser_NullPassword_ThrowsException() {
        //arrange
        User user = new User("user", "hash");
        when(userRepository.getByName("user")).thenReturn(user);

        //act & assert - NullPointerException from PasswordHasher
        assertThrows(NullPointerException.class, () ->
                userService.loginUser("user", null)
        );
    }

    @Test
    void updateUser_NullUser_ThrowsException() {
        //act & assert
        assertThrows(NullPointerException.class, () ->
                userService.updateUser(null)
        );
    }
}