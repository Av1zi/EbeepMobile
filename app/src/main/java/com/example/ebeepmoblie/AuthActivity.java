package com.example.ebeepmoblie;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Screens 2 and 3: this Activity hosts the two fragments
 * (RegisterFragment and LoginFragment) inside a ViewPager2,
 * so the user can swipe between them or press one of the tabs.
 */
public class AuthActivity extends AppCompatActivity {

    private static final int PAGE_REGISTER = 0;
    private static final int PAGE_LOGIN = 1;
    private static final String STATE_REGISTER_TAB = "register_tab";

    private ViewPager2 pager;
    private TextView txtTabRegister;
    private TextView txtTabLogin;
    private View underlineRegister;
    private View underlineLogin;
    private boolean registerTabSelected = true;

    // The email of the user that just registered (filled automatically on the login screen)
    private String registeredEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Keeps the purple header behind the status bar
        SystemBars.apply(this, findViewById(R.id.root), findViewById(R.id.header));

        txtTabRegister = findViewById(R.id.txtTabRegister);
        txtTabLogin = findViewById(R.id.txtTabLogin);
        underlineRegister = findViewById(R.id.underlineRegister);
        underlineLogin = findViewById(R.id.underlineLogin);

        pager = findViewById(R.id.pager);
        pager.setAdapter(new AuthPagerAdapter(this));

        // Swiping between the pages also moves the tabs
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                registerTabSelected = (position == PAGE_REGISTER);
                updateTabs();
            }
        });

        // Clicking a tab moves to its page
        findViewById(R.id.tabRegister).setOnClickListener(v -> showRegister());
        findViewById(R.id.tabLogin).setOnClickListener(v -> showLogin());

        // After a rotation the ViewPager2 restores the page by itself, only the tabs are refreshed
        if (savedInstanceState != null) {
            registerTabSelected = savedInstanceState.getBoolean(STATE_REGISTER_TAB, true);
        }
        updateTabs();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_REGISTER_TAB, registerTabSelected);
    }

    // Moves to the register fragment (screen 2)
    public void showRegister() {
        pager.setCurrentItem(PAGE_REGISTER, true);
    }

    // Moves to the login fragment (screen 3)
    public void showLogin() {
        showLogin(null);
    }

    // Moves to the login fragment with the email already filled in
    public void showLogin(String email) {
        registeredEmail = email;
        pager.setCurrentItem(PAGE_LOGIN, true);
    }

    // The email of the user that just registered (null when there is none)
    public String getRegisteredEmail() {
        return registeredEmail;
    }

    // Paints the selected tab in a dark color and shows its underline
    private void updateTabs() {
        int activeColor = ContextCompat.getColor(this, R.color.tab_active);
        int inactiveColor = ContextCompat.getColor(this, R.color.purple_light);

        txtTabRegister.setTextColor(registerTabSelected ? activeColor : inactiveColor);
        txtTabLogin.setTextColor(registerTabSelected ? inactiveColor : activeColor);
        underlineRegister.setVisibility(registerTabSelected ? View.VISIBLE : View.INVISIBLE);
        underlineLogin.setVisibility(registerTabSelected ? View.INVISIBLE : View.VISIBLE);
    }

    // Gives the pager its two pages: register and login
    private static class AuthPagerAdapter extends FragmentStateAdapter {

        AuthPagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == PAGE_REGISTER) {
                return new RegisterFragment();
            }
            return new LoginFragment();
        }
    }
}
