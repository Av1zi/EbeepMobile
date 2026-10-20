package com.ebeep.mobile.ui;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * The app is drawn behind the status bar and the navigation bar (the default on new Android),
 * so their heights are added as padding. Without this the clock and the battery
 * would cover the top of the screen.
 */
public class SystemBars {

    /** @param root the root view of the screen's layout */
    public static void apply(Activity activity, View root) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            // The status bar, the navigation bar and the camera cutout
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            // When the keyboard is open, the fields must not be hidden behind it
            Insets keyboard = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(bars.bottom, keyboard.bottom);

            view.setPadding(bars.left, bars.top, bars.right, bottom);
            return windowInsets;
        });
    }
}
