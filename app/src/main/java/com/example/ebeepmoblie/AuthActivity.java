package com.example.ebeepmoblie;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// Hosts the Register and Login fragments (screens 2 and 3).
public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Button btnTabRegister = findViewById(R.id.btnTabRegister);
        Button btnTabLogin = findViewById(R.id.btnTabLogin);

        btnTabRegister.setOnClickListener(v -> showRegister());
        btnTabLogin.setOnClickListener(v -> showLogin());

        // Show register first.
        if (savedInstanceState == null) {
            showRegister();
        }
    }

    // Show the register fragment.
    public void showRegister() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new RegisterFragment())
                .commit();
    }

    // Show the login fragment.
    public void showLogin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();
    }
}
