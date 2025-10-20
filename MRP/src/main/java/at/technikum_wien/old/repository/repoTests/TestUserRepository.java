package at.technikum_wien.old.repository.repoTests;

import at.technikum_wien.old.model.User;
import at.technikum_wien.old.repository.UserRepository;
import at.technikum_wien.old.util.PasswordHasher;

public class TestUserRepository {
    public static void main(String[] args) {
        UserRepository userRepo = new UserRepository();
        String hashedPWD= PasswordHasher.hashPassword("testpwd");
        User newUser = new User("testuser",hashedPWD);
        User savedUser=userRepo.save(newUser);

        if (savedUser != null) {
            System.out.println("User created successfully!");
            System.out.println("ID: " + newUser.getId());
            System.out.println("Username: " + newUser.getUsername());
        } else {
            System.out.println("Failed to create user");
        }

        //test with existing username
        User foundByName=userRepo.getByName("testuser");
        if (foundByName != null) {
            System.out.println("User found successfully! ID:  " + foundByName.getId());
        }else{
            System.out.println("Failed to find user by name");
        }

        //test with non-existing name
        User notFoundByName=userRepo.getByName("testuser2");
        if(notFoundByName==null){
            System.out.println("Correctly: User not found by name");
        }

        //test with existing ID
        User foundById = userRepo.getById(1);
        if (foundById != null) {
            System.out.println("Found by ID: " + foundById.getUsername());
        } else {
            System.out.println("User with ID 1 not found");
        }

        //test with non-existing ID
        User notFoundByID = userRepo.getById(999);
        if (notFoundByID == null) {
            System.out.println("Correctly: User not found by ID");
        }
    }
}
