package com.example.ebeepmoblie;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// Screen 4: shows all registered users.
public class UsersActivity extends AppCompatActivity {

    private ListView listUsers;
    private TextView txtEmpty;
    private TextView txtCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        listUsers = findViewById(R.id.listUsers);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtCount = findViewById(R.id.txtCount);
        Button btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUsers();
    }

    // Fill the list with all users from the store.
    private void showUsers() {
        if (UserStore.users.isEmpty()) {
            listUsers.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.VISIBLE);
            txtCount.setText("Registered users (0)");
        } else {
            listUsers.setVisibility(View.VISIBLE);
            txtEmpty.setVisibility(View.GONE);
            txtCount.setText("Registered users (" + UserStore.users.size() + ")");
            UsersAdapter adapter = new UsersAdapter(this, UserStore.users);
            listUsers.setAdapter(adapter);
        }
    }
}
