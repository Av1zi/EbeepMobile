package com.ebeep.mobile.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.PasswordHasher;
import com.ebeep.mobile.data.Session;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Screen 3: create a new account, then go straight to My Beepers. */
public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputLayout layoutUsername;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirm;
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private TextInputEditText editConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        SystemBars.apply(this, findViewById(R.id.root));

        setUpViews();
    }

    private void setUpViews() {
        // The top bar with a back arrow that returns to the login screen
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.register_title);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_back);
        toolbar.setNavigationContentDescription(R.string.cancel);
        toolbar.setNavigationOnClickListener(v -> finish());

        layoutUsername = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirm = findViewById(R.id.layoutConfirm);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editConfirm = findViewById(R.id.editConfirm);

        findViewById(R.id.btnRegister).setOnClickListener(v -> tryRegister());
        findViewById(R.id.btnGoLogin).setOnClickListener(v -> finish());
    }

    private void tryRegister() {
        layoutUsername.setError(null);
        layoutPassword.setError(null);
        layoutConfirm.setError(null);

        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();
        String confirm = editConfirm.getText().toString();

        if (!inputIsValid(username, password, confirm)) {
            return;
        }

        DatabaseHelper database = new DatabaseHelper(this);
        long userId = database.addUser(username, PasswordHasher.hash(password));
        database.close();

        // addUser returns -1 when the username already exists
        if (userId == -1) {
            layoutUsername.setError(getString(R.string.register_error_taken));
            return;
        }

        Session.logIn(this, userId, true);
        openBeepersScreen();
    }

    /** Checks the fields in order and shows the first problem under its field. */
    private boolean inputIsValid(String username, String password, String confirm) {
        if (username.isEmpty()) {
            layoutUsername.setError(getString(R.string.register_error_empty));
            return false;
        }
        if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.register_error_empty));
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            layoutPassword.setError(getString(R.string.register_error_short));
            return false;
        }
        if (!password.equals(confirm)) {
            layoutConfirm.setError(getString(R.string.register_error_mismatch));
            return false;
        }
        return true;
    }

    private void openBeepersScreen() {
        Intent intent = new Intent(this, BeepersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
