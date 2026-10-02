package com.example.ebeepmoblie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ebeep.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USERS = "users";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(database);
    }

    public boolean addUser(User user) {
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("password", user.getPassword());
        return getWritableDatabase().insert(TABLE_USERS, null, values) != -1;
    }

    // Deletes one user by its email
    public void deleteUser(String email) {
        getWritableDatabase().delete(TABLE_USERS, "email = ?", new String[]{email});
    }

    public User findByEmail(String email) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_USERS,
                new String[]{"name", "email", "password"},
                "email = ?",
                new String[]{email},
                null,
                null,
                null);

        try {
            if (cursor.moveToFirst()) {
                return new User(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2));
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                TABLE_USERS,
                new String[]{"name", "email", "password"},
                null,
                null,
                null,
                null,
                "name COLLATE NOCASE ASC");

        try {
            while (cursor.moveToNext()) {
                users.add(new User(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)));
            }
            return users;
        } finally {
            cursor.close();
        }
    }
}
