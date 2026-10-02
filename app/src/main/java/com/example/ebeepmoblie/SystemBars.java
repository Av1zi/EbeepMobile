package com.example.ebeepmoblie;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * The app is drawn behind the status bar, so the system bar heights are added as padding.
 * Without this the clock, the battery and the selfie camera overlap the text of the screen.
 */
public class SystemBars {

    /**
     * Adds the system bars as padding.
     *
     * @param root   the root view of the screen
     * @param header the purple header, so it stays behind the status bar (null when the screen has no header)
     */
    public static void apply(Activity activity, View root, View header) {
        // The app draws behind the system bars (this is the default on Android 15 and newer)
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            // The status bar, the navigation bar and the camera cutout
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            // The keyboard (when it opens the fields must not be covered)
            Insets keyboard = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(bars.bottom, keyboard.bottom);

            if (header == null) {
                // All the content is pushed below the status bar
                view.setPadding(0, bars.top, 0, bottom);
            } else {
                // The header is pushed down but keeps its purple color behind the status bar
                Object tag = header.getTag(R.id.header);
                int initialTopPadding = tag instanceof Integer ? (Integer) tag : header.getPaddingTop();
                if (tag == null) {
                    header.setTag(R.id.header, initialTopPadding);
                }
                header.setPadding(header.getPaddingLeft(), initialTopPadding + bars.top,
                        header.getPaddingRight(), header.getPaddingBottom());
                view.setPadding(0, 0, 0, bottom);
            }
            return windowInsets;
        });
    }
}
