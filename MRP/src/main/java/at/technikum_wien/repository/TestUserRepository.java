package at.technikum_wien.repository;

import at.technikum_wien.model.User;

public class TestUserRepository {
    public static void main(String[] args) {
        UserRepository userRepo = new UserRepository();
        User newUser = userRepo.createUser("testuser", "testpassword");

        if (newUser != null) {
            System.out.println("User created successfully!");
            System.out.println("ID: " + newUser.getId());
            System.out.println("Username: " + newUser.getUsername());
        } else {
            System.out.println("Failed to create user");
        }

        //test with existing username
        User foundByName=userRepo.getUserByUsername("testuser");
        if (foundByName != null) {
            System.out.println("User found successfully! ID:  " + foundByName.getId());
        }else{
            System.out.println("Failed to find user by name");
        }

        //test with non-existing name
        User notFoundByName=userRepo.getUserByUsername("testuser2");
        if(notFoundByName==null){
            System.out.println("Correctly: User not found by name");
        }

        //test with existing ID
        User foundById = userRepo.getUserById(1); // Die ID von deinem testuser
        if (foundById != null) {
            System.out.println("Found by ID: " + foundById.getUsername());
        } else {
            System.out.println("User with ID 1 not found");
        }

        //test with non-existing ID
        User notFoundByID = userRepo.getUserById(999);
        if (notFoundByID == null) {
            System.out.println("Correctly: User not found by ID");
        }
    }
}
