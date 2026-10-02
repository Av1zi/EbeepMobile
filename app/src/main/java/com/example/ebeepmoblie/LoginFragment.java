package com.example.ebeepmoblie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Screen 3: login.
 * The screen checks the email and the password against the registered users.
 */
public class LoginFragment extends Fragment {

    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        layoutEmail = view.findViewById(R.id.layoutEmail);
        layoutPassword = view.findViewById(R.id.layoutPassword);
        inputEmail = view.findViewById(R.id.inputEmail);
        inputPassword = view.findViewById(R.id.inputPassword);

        view.findViewById(R.id.btnLogin).setOnClickListener(v -> loginUser());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // If a user just registered, the email is filled in automatically
        String registeredEmail = ((AuthActivity) requireActivity()).getRegisteredEmail();
        if (registeredEmail != null && inputEmail.getText().toString().isEmpty()) {
            inputEmail.setText(registeredEmail);
        }
    }

    // Checks the details and opens the users screen
    private void loginUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString();

        clearErrors();

        if (email.isEmpty()) {
            showError(layoutEmail, getString(R.string.error_email_required));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(layoutEmail, getString(R.string.error_email_invalid));
            return;
        }
        if (password.isEmpty()) {
            showError(layoutPassword, getString(R.string.error_password_required));
            return;
        }

        // Search the user in the ArrayList of the registered users
        User user = UserStore.findByEmail(requireContext(), email);
        if (user == null) {
            showError(layoutEmail, getString(R.string.error_user_not_found));
            return;
        }
        if (!user.getPassword().equals(password)) {
            showError(layoutPassword, getString(R.string.error_wrong_password));
            return;
        }

        // Login succeeded
        UserStore.setCurrentUser(user);
        Toast.makeText(getContext(), R.string.msg_login_success, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(requireActivity(), UsersActivity.class));
    }

    // Shows a message below the field that has a problem
    private void showError(TextInputLayout layout, String message) {
        layout.setError(message);
        layout.requestFocus();
    }

    // Clears the messages of the previous check
    private void clearErrors() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);
    }
}
