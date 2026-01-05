package at.technikum_wien.services;

import at.technikum_wien.models.entities.User;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.database.repositories.TokenRepository;
import at.technikum_wien.security.PasswordHasher;
import java.util.List;
import java.util.Map;

public class UserService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    public UserService(UserRepository userRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    public User registerUser(String username, String password) {
        //check if user exists
        User existingUser = userRepository.getByName(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        //create new user
        String hashedPassword = PasswordHasher.hashPassword(password);
        User newUser = new User(username, hashedPassword);
        return userRepository.save(newUser);
    }

    public LoginResult loginUser(String username, String password) {
        User user = userRepository.getByName(username);
        if (user == null) {
            throw new SecurityException("Invalid username or password");
        }

        boolean passwordValid = PasswordHasher.checkPassword(password, user.getPasswordHash());
        if (!passwordValid) {
            throw new SecurityException("Invalid username or password");
        }

        String token = tokenRepository.createToken(user.getId(), user.getUsername());
        return new LoginResult(token, user.getId());
    }

    public static class LoginResult {
        public final String token;
        public final int userId;

        public LoginResult(String token, int userId) {
            this.token = token;
            this.userId = userId;
        }
    }

    public User updateUser(User user) {
        // Check if username is being changed and if it's already taken
        User existingUser = userRepository.getById(user.getId());
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }

        // If username is being changed, check if new username is available
        if (!existingUser.getUsername().equals(user.getUsername())) {
            User userWithNewUsername = userRepository.getByName(user.getUsername());
            if (userWithNewUsername != null && userWithNewUsername.getId() != user.getId()) {
                throw new IllegalArgumentException("Username already exists");
            }
        }

        // If favoriteGenre is not provided in the update, keep the existing one
        if (user.getFavoriteGenre() == null && existingUser.getFavoriteGenre() != null) {
            user.setFavoriteGenre(existingUser.getFavoriteGenre());
        }

        return userRepository.save(user);
    }

    public User getUserById(int id) {
        return userRepository.getById(id);
    }

}