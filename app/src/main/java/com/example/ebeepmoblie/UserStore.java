package com.example.ebeepmoblie;

import android.content.Context;

import java.util.ArrayList;

public class UserStore {

    public static ArrayList<User> users = new ArrayList<>();

    public static void load(Context context) {
        DatabaseHelper database = new DatabaseHelper(context);
        users = database.getAllUsers();
        database.close();
    }

    public static boolean add(Context context, User user) {
        DatabaseHelper database = new DatabaseHelper(context);
        boolean added = database.addUser(user);
        database.close();
        if (added) {
            users.add(user);
        }
        return added;
    }

    public static User findByEmail(Context context, String email) {
        DatabaseHelper database = new DatabaseHelper(context);
        User user = database.findByEmail(email);
        database.close();
        return user;
    }
}
