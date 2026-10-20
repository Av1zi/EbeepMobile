package com.ebeep.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.PasswordHasher;
import com.ebeep.mobile.data.Session;
import com.ebeep.mobile.data.User;
import com.google.android.material.textfield.TextInputEditText;

/** Screen 2: log in with an existing username and password. */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private CheckBox checkRemember;
    private TextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SystemBars.apply(this, findViewById(R.id.root));

        setUpViews();
    }

    private void setUpViews() {
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        checkRemember = findViewById(R.id.checkRemember);
        txtError = findViewById(R.id.txtError);

        findViewById(R.id.btnLogin).setOnClickListener(v -> tryLogin());
        findViewById(R.id.txtRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void tryLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            showError(R.string.login_error_empty);
            return;
        }

        DatabaseHelper database = new DatabaseHelper(this);
        User user = database.findUser(username);
        database.close();

        if (user == null || !user.getPasswordHash().equals(PasswordHasher.hash(password))) {
            showError(R.string.login_error_wrong);
            return;
        }

        Session.logIn(this, user.getId(), checkRemember.isChecked());
        openBeepersScreen();
    }

    private void openBeepersScreen() {
        Intent intent = new Intent(this, BeepersActivity.class);
        // Clear the back stack so Back from the beepers list doesn't return here
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showError(int messageId) {
        txtError.setText(messageId);
        txtError.setVisibility(View.VISIBLE);
    }
}
