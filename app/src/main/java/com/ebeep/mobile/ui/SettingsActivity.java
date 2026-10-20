package com.ebeep.mobile.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.AppSettings;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.Session;
import com.ebeep.mobile.data.User;
import com.ebeep.mobile.net.BeeperService;
import com.google.android.material.switchmaterial.SwitchMaterial;

/** Screen 13: settings, and the account actions (clear history, log out, delete account). */
public class SettingsActivity extends BaseActivity {

    @Override
    protected boolean showsConnection() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SystemBars.apply(this, findViewById(R.id.root));
        setUpToolbar(R.string.settings_title, true);

        showUsername();
        setUpSwitches();
        setUpButtons();
    }

    private void showUsername() {
        DatabaseHelper database = new DatabaseHelper(this);
        User user = database.findUserById(Session.getUserId(this));
        database.close();

        TextView txtUser = findViewById(R.id.txtUser);
        if (user != null) {
            txtUser.setText(getString(R.string.settings_logged_in_as, user.getUsername()));
        }
    }

    private void setUpSwitches() {
        SwitchMaterial switchNotifications = findViewById(R.id.switchNotifications);
        SwitchMaterial switchSound = findViewById(R.id.switchSound);
        SwitchMaterial switchDarkMode = findViewById(R.id.switchDarkMode);

        // The values are set before the listeners, so setting them doesn't trigger a save
        switchNotifications.setChecked(AppSettings.isNotificationsOn(this));
        switchSound.setChecked(AppSettings.isSoundOn(this));
        switchDarkMode.setChecked(AppSettings.isDarkMode(this));

        switchNotifications.setOnCheckedChangeListener((button, checked) ->
                AppSettings.setNotificationsOn(this, checked));
        switchSound.setOnCheckedChangeListener((button, checked) ->
                AppSettings.setSoundOn(this, checked));
        // Changing the theme recreates this screen, which is fine
        switchDarkMode.setOnCheckedChangeListener((button, checked) ->
                AppSettings.setDarkMode(this, checked));
    }

    private void setUpButtons() {
        findViewById(R.id.btnClearHistory).setOnClickListener(v -> confirmClearHistory());
        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogOut());
        findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> confirmDeleteAccount());
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_clear_title)
                .setMessage(R.string.settings_clear_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> clearHistory())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void clearHistory() {
        DatabaseHelper database = new DatabaseHelper(this);
        database.deleteGameRecords(Session.getUserId(this));
        database.close();
        Toast.makeText(this, R.string.settings_cleared, Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_delete_title)
                .setMessage(R.string.settings_delete_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteAccount() {
        // Stop the connection first: the beeper it uses is about to be deleted
        BeeperService.stop(this);

        // The cascade in the database also deletes the user's beepers and games
        DatabaseHelper database = new DatabaseHelper(this);
        database.deleteUser(Session.getUserId(this));
        database.close();

        logOut();
    }
}
