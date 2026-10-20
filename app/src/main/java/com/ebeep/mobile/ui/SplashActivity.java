package com.ebeep.mobile.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.Session;

/** Screen 1: the logo for a moment, then Login or My Beepers depending on the saved session. */
// Lint suggests the system SplashScreen API, but this screen is one of the planned
// screens of the project (screen 1), so it stays a normal activity.
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The app is in Hebrew, so every screen, dialog and date is right-to-left and in Hebrew,
        // even when the phone itself is set to English. It has to be called from a screen
        // (not from EbeepApp), and Android remembers it for the next times.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("he"));

        setContentView(R.layout.activity_splash);
        SystemBars.apply(this, findViewById(R.id.root));

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
