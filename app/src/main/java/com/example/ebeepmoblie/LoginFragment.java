package com.example.ebeepmoblie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

// Screen 3: login with email and password.
public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText inputEmail = view.findViewById(R.id.inputEmail);
        EditText inputPassword = view.findViewById(R.id.inputPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString();

            // All fields are required.
            if (email.isEmpty()) {
                inputEmail.setError("Please enter email");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.setError("Please enter a valid email");
                return;
            }
            if (password.isEmpty()) {
                inputPassword.setError("Please enter password");
                return;
            }

            // Check against registered users.
            User user = UserStore.findByEmail(email);
            if (user == null) {
                Toast.makeText(getActivity(), "No user found with this email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!user.getPassword().equals(password)) {
                Toast.makeText(getActivity(), "Wrong password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Login OK: greet and open the users list.
            Toast.makeText(getActivity(), "Welcome, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), UsersActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
