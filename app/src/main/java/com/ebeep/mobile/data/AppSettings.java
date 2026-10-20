package com.ebeep.mobile.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/** The user's settings from the settings screen, saved in SharedPreferences. */
public class AppSettings {

    private static final String PREFS = "settings";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_DARK_MODE = "dark_mode";

    public static boolean isNotificationsOn(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFICATIONS, true);
    }

    public static void setNotificationsOn(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS, on).apply();
    }

    public static boolean isSoundOn(Context context) {
        return prefs(context).getBoolean(KEY_SOUND, true);
    }

    public static void setSoundOn(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY_SOUND, on).apply();
    }

    public static boolean isDarkMode(Context context) {
        return prefs(context).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, on).apply();
        applyTheme(context);
    }

    /** Switches the whole app between the light and the dark theme. */
    public static void applyTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(isDarkMode(context)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
