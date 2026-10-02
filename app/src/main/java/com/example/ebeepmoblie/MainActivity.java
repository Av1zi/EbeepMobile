package com.example.ebeepmoblie;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// Screen 1: main welcome screen.
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGo = findViewById(R.id.btnGo);
        btnGo.setOnClickListener(v -> {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        });
    }
}
