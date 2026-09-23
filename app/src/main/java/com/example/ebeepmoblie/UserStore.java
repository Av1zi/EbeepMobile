package com.example.ebeepmoblie;

import java.util.ArrayList;

// Shared list of all registered users.
// Static so every screen uses the same list.
public class UserStore {

    public static ArrayList<User> users = new ArrayList<>();

    // Find a user by email. Returns null if not found.
    public static User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }
}
