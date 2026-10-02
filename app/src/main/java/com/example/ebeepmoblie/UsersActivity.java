package com.example.ebeepmoblie;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Screen 4: shows the details of the user that logged in
 * and the list of all the users that registered, with a name and an email in every row.
 */
public class UsersActivity extends AppCompatActivity {

    private ListView listUsers;
    private TextView txtEmpty;
    private TextView txtCount;
    private TextView txtUserName;
    private TextView txtUserEmail;
    private UsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Keeps the purple header behind the status bar
        SystemBars.apply(this, findViewById(R.id.root), findViewById(R.id.header));

        listUsers = findViewById(R.id.listUsers);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtCount = findViewById(R.id.txtCount);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);

        // Back to the main screen
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUsers();
    }

    // Fills the screen with the details and the list of the users
    private void showUsers() {
        // Reads the users from the database into the ArrayList
        UserStore.load(this);

        // Details of the user that logged in
        User currentUser = UserStore.getCurrentUser();
        if (currentUser != null) {
            txtUserName.setText(getString(R.string.label_name, currentUser.getName()));
            txtUserEmail.setText(getString(R.string.label_email, currentUser.getEmail()));
        }

        txtCount.setText(getString(R.string.users_count, UserStore.users.size()));

        // No users to show yet
        if (UserStore.users.isEmpty()) {
            listUsers.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.VISIBLE);
            return;
        }

        listUsers.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        adapter = new UsersAdapter(this, UserStore.users);
        listUsers.setAdapter(adapter);

        // Personal addition: a long press on a user asks to delete it
        listUsers.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(adapter.getItem(position));
            return true;
        });
    }

    // Asks the user before deleting
    private void confirmDelete(User user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, user.getName()))
                .setPositiveButton(R.string.delete_yes, (dialog, which) -> {
                    UserStore.remove(this, user);
                    Toast.makeText(this, R.string.msg_deleted, Toast.LENGTH_SHORT).show();
                    showUsers();
                })
                .setNegativeButton(R.string.delete_no, null)
                .show();
    }
}
