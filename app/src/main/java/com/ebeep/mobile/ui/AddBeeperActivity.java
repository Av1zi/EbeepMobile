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
import com.ebeep.mobile.net.BeeperService;
import com.ebeep.mobile.net.Protocol;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Screen 5: add a beeper, or edit one that was added before.
 * The connection is tested before the beeper is saved.
 */
public class AddBeeperActivity extends BaseActivity {

    /** Intent extra: the row id of the beeper to edit. Without it the screen adds a new beeper. */
    public static final String EXTRA_EDIT_BEEPER = "edit_beeper";

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

    // The beeper being edited, or null when adding a new one
    private Beeper editedBeeper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beeper);
        SystemBars.apply(this, findViewById(R.id.root));

        long editId = getIntent().getLongExtra(EXTRA_EDIT_BEEPER, -1);
        if (editId != -1) {
            DatabaseHelper database = new DatabaseHelper(this);
            editedBeeper = database.getBeeper(editId);
            database.close();
        }

        setUpToolbar(editedBeeper == null ? R.string.add_title : R.string.edit_title, true);
        setUpViews();
        if (editedBeeper != null) {
            fillFields(editedBeeper);
        }
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

        // Soft keys: Cancel on one side, Connect (or Save) in the middle
        setUpSoftKey(R.id.softKeyStart, R.string.cancel, v -> finish());
        setUpSoftKey(R.id.softKeyCenter, editedBeeper == null ? R.string.add_button : R.string.edit_button,
                v -> tryConnect());
        hideSoftKey(R.id.softKeyEnd);
        btnConnect = findViewById(R.id.softKeyCenter);
    }

    /** Shows the saved details. The ids are shown without "Beeper_", which the field already shows. */
    private void fillFields(Beeper beeper) {
        editNickname.setText(beeper.getNickname());
        editBeeperId.setText(withoutPrefix(beeper.getBeeperId()));
        editPhoneId.setText(withoutPrefix(beeper.getPhoneId()));
        editPassword.setText(beeper.getMqttPassword());
    }

    private String withoutPrefix(String deviceId) {
        if (deviceId.startsWith(Protocol.DEVICE_PREFIX)) {
            return deviceId.substring(Protocol.DEVICE_PREFIX.length());
        }
        return deviceId;
    }

    private void tryConnect() {
        layoutNickname.setError(null);
        layoutBeeperId.setError(null);
        layoutPhoneId.setError(null);
        layoutPassword.setError(null);

        String nickname = editNickname.getText().toString().trim();
        String beeperNumber = editBeeperId.getText().toString().trim();
        String phoneNumber = editPhoneId.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (!inputIsValid(nickname, beeperNumber, phoneNumber, password)) {
            return;
        }

        // The user only types what comes after "Beeper_"
        String beeperId = Protocol.DEVICE_PREFIX + beeperNumber;
        String phoneId = Protocol.DEVICE_PREFIX + phoneNumber;
        Beeper beeper = new Beeper(Session.getUserId(this), nickname, beeperId, phoneId, password);
        testConnectionAndSave(beeper);
    }

    private boolean inputIsValid(String nickname, String beeperNumber, String phoneNumber, String password) {
        boolean valid = true;
        if (nickname.isEmpty()) {
            layoutNickname.setError(getString(R.string.add_error_empty));
            valid = false;
        }
        if (beeperNumber.isEmpty()) {
            layoutBeeperId.setError(getString(R.string.add_error_empty));
            valid = false;
        }
        if (phoneNumber.isEmpty()) {
            layoutPhoneId.setError(getString(R.string.add_error_empty));
            valid = false;
        }
        if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.add_error_empty));
            valid = false;
        }
        if (valid && beeperNumber.equals(phoneNumber)) {
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

        if (!success) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.add_failed_title)
                    .setMessage(R.string.add_failed_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }

        if (editedBeeper == null) {
            DatabaseHelper database = new DatabaseHelper(this);
            database.addBeeper(beeper);
            database.close();
            Toast.makeText(this, R.string.add_saved, Toast.LENGTH_SHORT).show();
        } else {
            saveChanges(beeper);
            Toast.makeText(this, R.string.edit_saved, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void saveChanges(Beeper beeper) {
        DatabaseHelper database = new DatabaseHelper(this);
        database.updateBeeper(editedBeeper.getId(), beeper);
        database.close();

        // If this beeper is connected now, connect again with the new details
        Beeper connected = BeeperConnection.get().getBeeper();
        if (connected != null && connected.getId() == editedBeeper.getId()) {
            BeeperConnection.get().disconnect();
            BeeperService.start(this, editedBeeper.getId());
        }
    }
}
