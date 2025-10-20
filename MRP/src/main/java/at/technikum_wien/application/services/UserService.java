package at.technikum_wien.application.services;

import at.technikum_wien.domain.entities.User;
import at.technikum_wien.infrastructure.repositories.UserRepository;
import at.technikum_wien.infrastructure.repositories.TokenRepository;
import at.technikum_wien.infrastructure.security.PasswordHasher;

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
}