package com.ebeep.mobile.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Remembers who is logged in and which beeper is open.
 * With "remember me" the user id is saved in SharedPreferences, so it survives closing the app.
 * Without it, the id is kept only in memory until the app is closed.
 */
public class Session {

    private static final String PREFS = "session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_BEEPER_ID = "beeper_row_id";

    private static final long NONE = -1;

    // The logged in user for this run of the app
    private static long currentUserId = NONE;

    public static void logIn(Context context, long userId, boolean remember) {
        currentUserId = userId;
        SharedPreferences.Editor editor = prefs(context).edit();
        if (remember) {
            editor.putLong(KEY_USER_ID, userId);
        } else {
            editor.remove(KEY_USER_ID);
        }
        editor.apply();
    }

    /** The logged in user's id, or -1 when nobody is logged in. */
    public static long getUserId(Context context) {
        if (currentUserId == NONE) {
            currentUserId = prefs(context).getLong(KEY_USER_ID, NONE);
        }
        return currentUserId;
    }

    public static boolean isLoggedIn(Context context) {
        return getUserId(context) != NONE;
    }

    public static void logOut(Context context) {
        currentUserId = NONE;
        prefs(context).edit().clear().apply();
    }

    public static void setCurrentBeeper(Context context, long beeperRowId) {
        prefs(context).edit().putLong(KEY_BEEPER_ID, beeperRowId).apply();
    }

    /** The row id of the beeper that is open, or -1. */
    public static long getCurrentBeeper(Context context) {
        return prefs(context).getLong(KEY_BEEPER_ID, NONE);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
