package com.example.ebeepmoblie;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Screen 1: main screen with the app image, the student details,
 * a greeting and a button that moves to the register screen.
 */
public class MainActivity extends AppCompatActivity {

    private TextView txtWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keeps the content below the status bar and above the navigation bar
        SystemBars.apply(this, findViewById(R.id.root), null);

        txtWelcome = findViewById(R.id.txtWelcome);

        // The button moves to the register / login screen
        findViewById(R.id.btnGo).setOnClickListener(v ->
                startActivity(new Intent(this, AuthActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The greeting shows the name of the user that logged in (or a guest)
        User currentUser = UserStore.getCurrentUser();
        if (currentUser == null) {
            txtWelcome.setText(R.string.greeting_guest);
        } else {
            txtWelcome.setText(getString(R.string.greeting_user, currentUser.getName()));
        }
    }
}
