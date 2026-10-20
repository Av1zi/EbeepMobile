package com.ebeep.mobile;

import android.app.Application;

import com.ebeep.mobile.data.AppSettings;
import com.ebeep.mobile.net.Notifications;

/**
 * Runs once when the app process starts, before any screen opens.
 * Sets up the things the whole app shares.
 */
public class EbeepApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppSettings.applyTheme(this);
        Notifications.createChannels(this);
    }
}
