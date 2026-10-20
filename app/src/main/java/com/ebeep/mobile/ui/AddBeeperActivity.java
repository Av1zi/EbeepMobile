package com.ebeep.mobile.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.Beeper;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.Session;
import com.ebeep.mobile.net.BeeperConnection;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Screen 5: add a beeper. The connection is tested before the beeper is saved. */
public class AddBeeperActivity extends BaseActivity {

    private TextInputLayout layoutNickname;
    private TextInputLayout layoutBeeperId;
    private TextInputLayout layoutPhoneId;
    private TextInputLayout layoutPassword;
    private TextInputEditText editNickname;
    private TextInputEditText editBeeperId;
    private TextInputEditText editPhoneId;
    private TextInputEditText editPassword;
    private MaterialButton btnConnect;
    private View progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beeper);
        SystemBars.apply(this, findViewById(R.id.root));
        setUpToolbar(R.string.add_title, true);

        setUpViews();
    }

    private void setUpViews() {
        layoutNickname = findViewById(R.id.layoutNickname);
        layoutBeeperId = findViewById(R.id.layoutBeeperId);
        layoutPhoneId = findViewById(R.id.layoutPhoneId);
        layoutPassword = findViewById(R.id.layoutPassword);
        editNickname = findViewById(R.id.editNickname);
        editBeeperId = findViewById(R.id.editBeeperId);
        editPhoneId = findViewById(R.id.editPhoneId);
        editPassword = findViewById(R.id.editPassword);
        progress = findViewById(R.id.progress);

        // Soft keys: Cancel on one side, Connect in the middle
        setUpSoftKey(R.id.softKeyStart, R.string.cancel, v -> finish());
        setUpSoftKey(R.id.softKeyCenter, R.string.add_button, v -> tryConnect());
        hideSoftKey(R.id.softKeyEnd);
        btnConnect = findViewById(R.id.softKeyCenter);
    }

    private void tryConnect() {
        layoutNickname.setError(null);
        layoutBeeperId.setError(null);
        layoutPhoneId.setError(null);
        layoutPassword.setError(null);

        String nickname = editNickname.getText().toString().trim();
        String beeperId = editBeeperId.getText().toString().trim();
        String phoneId = editPhoneId.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (!inputIsValid(nickname, beeperId, phoneId, password)) {
            return;
        }

        Beeper beeper = new Beeper(Session.getUserId(this), nickname, beeperId, phoneId, password);
        testConnectionAndSave(beeper);
    }

    private boolean inputIsValid(String nickname, String beeperId, String phoneId, String password) {
        boolean valid = true;
        if (nickname.isEmpty()) {
            layoutNickname.setError(getString(R.string.add_error_empty));
            valid = false;
        }
        if (beeperId.isEmpty()) {
            layoutBeeperId.setError(getString(R.string.add_error_empty));
            valid = false;
        }
        if (phoneId.isEmpty()) {
            layoutPhoneId.setError(getString(R.string.add_error_empty));
            valid = false;
        }
        if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.add_error_empty));
            valid = false;
        }
        if (valid && beeperId.equals(phoneId)) {
            layoutPhoneId.setError(getString(R.string.add_error_same_ids));
            valid = false;
        }
        return valid;
    }

    /** The network call runs on its own thread, because the main thread must not wait for the network. */
    private void testConnectionAndSave(Beeper beeper) {
        progress.setVisibility(View.VISIBLE);
        btnConnect.setEnabled(false);

        new Thread(() -> {
            boolean success = BeeperConnection.testLogin(beeper.getPhoneId(), beeper.getMqttPassword());
            // Views can only be changed on the main thread
            runOnUiThread(() -> onTestFinished(success, beeper));
        }).start();
    }

    private void onTestFinished(boolean success, Beeper beeper) {
        // The user may have left the screen while we were waiting
        if (isFinishing()) {
            return;
        }
        progress.setVisibility(View.GONE);
        btnConnect.setEnabled(true);

        if (success) {
            DatabaseHelper database = new DatabaseHelper(this);
            database.addBeeper(beeper);
            database.close();
            Toast.makeText(this, R.string.add_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.add_failed_title)
                    .setMessage(R.string.add_failed_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    }
}
