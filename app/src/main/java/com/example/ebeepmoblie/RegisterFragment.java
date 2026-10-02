package com.example.ebeepmoblie;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

// Screen 2: register a new user.
public class RegisterFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        EditText inputName = view.findViewById(R.id.inputName);
        EditText inputEmail = view.findViewById(R.id.inputEmail);
        EditText inputPassword = view.findViewById(R.id.inputPassword);
        EditText inputConfirm = view.findViewById(R.id.inputConfirm);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString();
            String confirm = inputConfirm.getText().toString();

            // All fields are required.
            if (name.isEmpty()) {
                inputName.setError("Please enter username");
                return;
            }
            if (email.isEmpty()) {
                inputEmail.setError("Please enter email");
                return;
            }
            // Email must be valid.
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.setError("Please enter a valid email");
                return;
            }
            if (password.isEmpty()) {
                inputPassword.setError("Please enter password");
                return;
            }
            if (confirm.isEmpty()) {
                inputConfirm.setError("Please confirm password");
                return;
            }
            // Passwords must match.
            if (!password.equals(confirm)) {
                inputConfirm.setError("Passwords do not match");
                return;
            }
            if (UserStore.findByEmail(requireContext(), email) != null) {
                inputEmail.setError("This email is already registered");
                return;
            }

            if (!UserStore.add(requireContext(), new User(name, email, password))) {
                Toast.makeText(getActivity(), "Could not save user", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getActivity(), "Registered successfully", Toast.LENGTH_SHORT).show();

            // Move to login after registering.
            ((AuthActivity) getActivity()).showLogin();
        });

        return view;
    }
}
