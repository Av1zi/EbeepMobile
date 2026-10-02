package com.example.ebeepmoblie;

import android.content.Context;

import java.util.ArrayList;

/**
 * Holds all the registered users in one ArrayList and keeps them in the database too,
 * so the list stays after the app is closed.
 */
public class UserStore {

    // All the registered users
    public static ArrayList<User> users = new ArrayList<>();

    // The user that is currently logged in (null when nobody is logged in)
    private static User currentUser;

    // Reads all the users from the database into the ArrayList
    public static void load(Context context) {
        DatabaseHelper database = new DatabaseHelper(context);
        users = database.getAllUsers();
        database.close();
    }

    // Adds a new user to the ArrayList and to the database
    public static boolean add(Context context, User user) {
        // No duplicate emails
        if (findByEmail(context, user.getEmail()) != null) {
            return false;
        }

        DatabaseHelper database = new DatabaseHelper(context);
        boolean added = database.addUser(user);
        database.close();

        if (added) {
            users.add(user);
        }
        return added;
    }

    // Searches the ArrayList for a user with this email
    public static User findByEmail(Context context, String email) {
        load(context);
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    // Removes a user from the ArrayList and from the database
    public static void remove(Context context, User user) {
        DatabaseHelper database = new DatabaseHelper(context);
        database.deleteUser(user.getEmail());
        database.close();

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equalsIgnoreCase(user.getEmail())) {
                users.remove(i);
                break;
            }
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}
