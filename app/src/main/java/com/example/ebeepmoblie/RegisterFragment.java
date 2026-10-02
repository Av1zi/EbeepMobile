package com.example.ebeepmoblie;

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
 * Screen 2: register a new user.
 * The screen checks all the fields and saves the user in the ArrayList.
 */
public class RegisterFragment extends Fragment {

    private static final int MAX_NAME_LENGTH = 30;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputLayout layoutName;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirm;

    private TextInputEditText inputName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        layoutName = view.findViewById(R.id.layoutName);
        layoutEmail = view.findViewById(R.id.layoutEmail);
        layoutPassword = view.findViewById(R.id.layoutPassword);
        layoutConfirm = view.findViewById(R.id.layoutConfirm);

        inputName = view.findViewById(R.id.inputName);
        inputEmail = view.findViewById(R.id.inputEmail);
        inputPassword = view.findViewById(R.id.inputPassword);
        inputConfirm = view.findViewById(R.id.inputConfirm);

        view.findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());

        return view;
    }

    // Checks the fields and saves the new user
    private void registerUser() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString();
        String confirm = inputConfirm.getText().toString();

        clearErrors();

        // All the fields are required
        if (name.isEmpty()) {
            showError(layoutName, getString(R.string.error_name_required));
            return;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            showError(layoutName, getString(R.string.error_name_too_long));
            return;
        }
        if (email.isEmpty()) {
            showError(layoutEmail, getString(R.string.error_email_required));
            return;
        }
        // The email must look like a real email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(layoutEmail, getString(R.string.error_email_invalid));
            return;
        }
        if (password.isEmpty()) {
            showError(layoutPassword, getString(R.string.error_password_required));
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError(layoutPassword, getString(R.string.error_password_short));
            return;
        }
        if (confirm.isEmpty()) {
            showError(layoutConfirm, getString(R.string.error_confirm_required));
            return;
        }
        // The confirmation must be the same password
        if (!password.equals(confirm)) {
            showError(layoutConfirm, getString(R.string.error_confirm_mismatch));
            return;
        }
        // The email must not be used by another user
        if (UserStore.findByEmail(requireContext(), email) != null) {
            showError(layoutEmail, getString(R.string.error_email_taken));
            return;
        }

        // Save the user in the ArrayList (and in the database)
        if (!UserStore.add(requireContext(), new User(name, email, password))) {
            Toast.makeText(getContext(), R.string.error_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), R.string.msg_registered, Toast.LENGTH_SHORT).show();

        // Move to the login screen with the email already filled in
        ((AuthActivity) requireActivity()).showLogin(email);
    }

    // Shows a message below the field that has a problem
    private void showError(TextInputLayout layout, String message) {
        layout.setError(message);
        layout.requestFocus();
    }

    // Clears the messages of the previous check
    private void clearErrors() {
        layoutName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirm.setError(null);
    }
}
