package com.ebeep.mobile.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The app's SQLite database: users, the beepers each user connected,
 * and the results of the games against each beeper.
 * A user has many beepers, and a beeper has many game records.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ebeep.db";
    private static final int DATABASE_VERSION = 1;

    // The columns read for a Beeper, in the order readBeeper expects them
    private static final String[] BEEPER_COLUMNS = {
            "id", "user_id", "nickname", "beeper_id", "phone_id", "mqtt_password",
            "last_message", "last_message_mine", "last_message_time"};

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Foreign keys are off by default in SQLite. Without this, ON DELETE CASCADE does nothing.
    @Override
    public void onConfigure(SQLiteDatabase database) {
        database.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL)");

        database.execSQL("CREATE TABLE beepers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, " +
                "nickname TEXT NOT NULL, " +
                "beeper_id TEXT NOT NULL, " +
                "phone_id TEXT NOT NULL, " +
                "mqtt_password TEXT NOT NULL, " +
                "last_message TEXT, " +
                "last_message_mine INTEGER, " +
                "last_message_time INTEGER)");

        database.execSQL("CREATE TABLE game_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "beeper_id INTEGER NOT NULL REFERENCES beepers(id) ON DELETE CASCADE, " +
                "game TEXT NOT NULL, " +
                "result TEXT NOT NULL, " +
                "played_at INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS game_records");
        database.execSQL("DROP TABLE IF EXISTS beepers");
        database.execSQL("DROP TABLE IF EXISTS users");
        onCreate(database);
    }

    // ---------- Users ----------

    /** Adds a user. Returns the new id, or -1 if the username is already taken. */
    public long addUser(String username, String passwordHash) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password_hash", passwordHash);
        return getWritableDatabase().insert("users", null, values);
    }

    public User findUser(String username) {
        return queryOneUser("username = ?", username);
    }

    public User findUserById(long id) {
        return queryOneUser("id = ?", String.valueOf(id));
    }

    private User queryOneUser(String where, String value) {
        Cursor cursor = getReadableDatabase().query("users",
                new String[]{"id", "username", "password_hash"},
                where, new String[]{value}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return new User(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    /** Deletes a user. The cascade also deletes the user's beepers and their games. */
    public void deleteUser(long id) {
        getWritableDatabase().delete("users", "id = ?", new String[]{String.valueOf(id)});
    }

    // ---------- Beepers ----------

    public long addBeeper(Beeper beeper) {
        ContentValues values = new ContentValues();
        values.put("user_id", beeper.getUserId());
        values.put("nickname", beeper.getNickname());
        values.put("beeper_id", beeper.getBeeperId());
        values.put("phone_id", beeper.getPhoneId());
        values.put("mqtt_password", beeper.getMqttPassword());
        return getWritableDatabase().insert("beepers", null, values);
    }

    /** All the beepers of one user, sorted by nickname. */
    public ArrayList<Beeper> getBeepers(long userId) {
        ArrayList<Beeper> beepers = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query("beepers", BEEPER_COLUMNS,
                "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "nickname COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                beepers.add(readBeeper(cursor));
            }
            return beepers;
        } finally {
            cursor.close();
        }
    }

    public Beeper getBeeper(long id) {
        Cursor cursor = getReadableDatabase().query("beepers", BEEPER_COLUMNS,
                "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return readBeeper(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private Beeper readBeeper(Cursor cursor) {
        return new Beeper(
                cursor.getLong(0),
                cursor.getLong(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getInt(7) == 1,
                cursor.getLong(8));
    }

    /** Saves new details for a beeper. The last message and the games stay as they are. */
    public void updateBeeper(long id, Beeper newDetails) {
        ContentValues values = new ContentValues();
        values.put("nickname", newDetails.getNickname());
        values.put("beeper_id", newDetails.getBeeperId());
        values.put("phone_id", newDetails.getPhoneId());
        values.put("mqtt_password", newDetails.getMqttPassword());
        getWritableDatabase().update("beepers", values, "id = ?", new String[]{String.valueOf(id)});
    }

    /** Deletes a beeper. The cascade also deletes its games. */
    public void deleteBeeper(long id) {
        getWritableDatabase().delete("beepers", "id = ?", new String[]{String.valueOf(id)});
    }

    /** Only the last message is kept, to protect the user's privacy. */
    public void saveLastMessage(long beeperRowId, String text, boolean mine) {
        ContentValues values = new ContentValues();
        values.put("last_message", text);
        values.put("last_message_mine", mine ? 1 : 0);
        values.put("last_message_time", System.currentTimeMillis());
        getWritableDatabase().update("beepers", values, "id = ?",
                new String[]{String.valueOf(beeperRowId)});
    }

    // ---------- Game records ----------

    public void addGameRecord(long beeperRowId, String game, String result) {
        ContentValues values = new ContentValues();
        values.put("beeper_id", beeperRowId);
        values.put("game", game);
        values.put("result", result);
        values.put("played_at", System.currentTimeMillis());
        getWritableDatabase().insert("game_records", null, values);
    }

    /** All the games against one beeper, newest first. */
    public ArrayList<GameRecord> getGameRecords(long beeperRowId) {
        ArrayList<GameRecord> records = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query("game_records",
                new String[]{"id", "game", "result", "played_at"},
                "beeper_id = ?", new String[]{String.valueOf(beeperRowId)},
                null, null, "played_at DESC");
        try {
            while (cursor.moveToNext()) {
                records.add(new GameRecord(cursor.getLong(0), cursor.getString(1),
                        cursor.getString(2), cursor.getLong(3)));
            }
            return records;
        } finally {
            cursor.close();
        }
    }

    /**
     * Counts wins, losses and draws for each game against one beeper.
     * The key is the game ("TTT"/"C4"), the value is {wins, losses, draws}.
     */
    public HashMap<String, int[]> getGameSummary(long beeperRowId) {
        HashMap<String, int[]> summary = new HashMap<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT game, result, COUNT(*) FROM game_records " +
                        "WHERE beeper_id = ? GROUP BY game, result",
                new String[]{String.valueOf(beeperRowId)});
        try {
            while (cursor.moveToNext()) {
                String game = cursor.getString(0);
                String result = cursor.getString(1);
                int count = cursor.getInt(2);

                if (!summary.containsKey(game)) {
                    summary.put(game, new int[3]);
                }
                int[] counts = summary.get(game);
                if (result.equals(GameRecord.WIN)) {
                    counts[0] = count;
                } else if (result.equals(GameRecord.LOSS)) {
                    counts[1] = count;
                } else {
                    counts[2] = count;
                }
            }
            return summary;
        } finally {
            cursor.close();
        }
    }

    /** Deletes the game history of all the beepers of one user. */
    public void deleteGameRecords(long userId) {
        getWritableDatabase().execSQL(
                "DELETE FROM game_records WHERE beeper_id IN " +
                        "(SELECT id FROM beepers WHERE user_id = ?)",
                new Object[]{userId});
    }
}
