package com.ebeep.mobile;

/**
 * Counts the screens that are open right now.
 * The service uses it to decide between showing a dialog (a screen is open)
 * and showing a notification (the app is in the background).
 */
public class OpenScreens {

    private static int count = 0;

    public static void screenOpened() {
        count++;
    }

    public static void screenClosed() {
        if (count > 0) {
            count--;
        }
    }

    public static boolean isAnyOpen() {
        return count > 0;
    }
}
