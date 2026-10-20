package com.ebeep.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.AppSettings;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.Session;
import com.ebeep.mobile.net.Notifications;

/** Screen 1: the logo for a moment, then Login or My Beepers depending on the saved session. */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The theme must be set before the screen is drawn
        AppSettings.applyTheme(this);
        setContentView(R.layout.activity_splash);
        SystemBars.apply(this, findViewById(R.id.root));

        Notifications.createChannels(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::openNextScreen, SPLASH_DELAY_MS);
    }

    private void openNextScreen() {
        Class<?> nextScreen = LoginActivity.class;
        if (Session.isLoggedIn(this) && userStillExists()) {
            nextScreen = BeepersActivity.class;
        }
        startActivity(new Intent(this, nextScreen));
        // So Back doesn't return to the splash
        finish();
    }

    /** The account may have been deleted since the last run. */
    private boolean userStillExists() {
        DatabaseHelper database = new DatabaseHelper(this);
        boolean exists = database.findUserById(Session.getUserId(this)) != null;
        database.close();

        if (!exists) {
            Session.logOut(this);
        }
        return exists;
    }
}
