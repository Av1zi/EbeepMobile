package com.ebeep.mobile.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ebeep.mobile.OpenScreens;
import com.ebeep.mobile.R;
import com.ebeep.mobile.data.Beeper;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.Session;
import com.ebeep.mobile.net.BeeperConnection;
import com.ebeep.mobile.net.BeeperService;
import com.ebeep.mobile.net.NetworkReceiver;
import com.ebeep.mobile.net.Protocol;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

/**
 * Base for every screen after login. While the screen is open it listens to the beeper,
 * shows the game invitation dialog (screen 9), and shows a banner when the phone loses the network.
 * Screens that handle beeper messages override onBeeperMessage and call super first.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements BeeperConnection.Listener, NetworkReceiver.Listener {

    /** Intent extra for the game screens: true when the phone makes the first move. */
    public static final String EXTRA_I_START = "i_start";

    private final NetworkReceiver networkReceiver = new NetworkReceiver(this);
    private Snackbar networkBanner;
    private AlertDialog invitationDialog;

    @Override
    protected void onResume() {
        super.onResume();
        OpenScreens.screenOpened();
        BeeperConnection.get().setScreenListener(this);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // An invitation that arrived while the app was in the background
        String game = BeeperConnection.get().takePendingInvitation();
        if (game != null) {
            showInvitationDialog(game);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        OpenScreens.screenClosed();
        BeeperConnection.get().setScreenListener(null);
        unregisterReceiver(networkReceiver);
    }

    /** Sets the toolbar from the layout (id "toolbar") as the screen's top bar. */
    protected void setUpToolbar(CharSequence title, boolean showBackArrow) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(showBackArrow);
        if (showBackArrow) {
            // The arrow does the same as the phone's Back button
            toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }

    protected void setUpToolbar(@StringRes int titleId, boolean showBackArrow) {
        setUpToolbar(getString(titleId), showBackArrow);
    }

    /** The beeper that is open right now (from the session), or null. */
    protected Beeper loadCurrentBeeper() {
        DatabaseHelper database = new DatabaseHelper(this);
        Beeper beeper = database.getBeeper(Session.getCurrentBeeper(this));
        database.close();
        return beeper;
    }

    /** Asks "are you sure?" and logs out on yes. Used by the beepers list and the settings. */
    protected void confirmLogOut() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> logOut())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /** Stops the connection, forgets the user and goes back to the login screen. */
    protected void logOut() {
        BeeperService.stop(this);
        Session.logOut(this);

        Intent intent = new Intent(this, LoginActivity.class);
        // Clear the back stack so Back from the login screen doesn't return into the account
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ---- Beeper messages ----

    @Override
    public void onBeeperMessage(String subTopic, String text) {
        if (!subTopic.equals(Protocol.GAMES)) {
            return;
        }
        if (text.equals(Protocol.TTT + Protocol.START)) {
            showInvitationDialog(Protocol.TTT);
        } else if (text.equals(Protocol.C4 + Protocol.START)) {
            showInvitationDialog(Protocol.C4);
        } else if (text.endsWith(Protocol.LEFT) && invitationDialog != null) {
            // The beeper gave up on its invitation before we answered
            invitationDialog.dismiss();
            invitationDialog = null;
        }
    }

    @Override
    public void onConnectionChanged(boolean connected) {
        // Screens that show the connection state override this
    }

    private void showInvitationDialog(String game) {
        Beeper beeper = BeeperConnection.get().getBeeper();
        if (beeper == null || isFinishing()) {
            return;
        }
        if (invitationDialog != null) {
            invitationDialog.dismiss();
        }

        String gameName = getString(game.equals(Protocol.TTT)
                ? R.string.game_tic_tac_toe : R.string.game_connect_four);

        invitationDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.invite_title)
                .setMessage(getString(R.string.invite_message, beeper.getNickname(), gameName))
                .setCancelable(false)
                .setPositiveButton(R.string.invite_accept, (dialog, which) -> {
                    BeeperConnection.get().sendToBeeper(Protocol.GAMES, game + Protocol.START_ACK, false);
                    openGame(game, false);
                })
                .setNegativeButton(R.string.invite_decline, (dialog, which) ->
                        BeeperConnection.get().sendToBeeper(Protocol.GAMES, game + Protocol.LEFT, false))
                .show();
    }

    /** Opens the game screen. iStart is true when the phone makes the first move. */
    protected void openGame(String game, boolean iStart) {
        Class<?> screen = game.equals(Protocol.TTT) ? TicTacToeActivity.class : ConnectFourActivity.class;
        Intent intent = new Intent(this, screen);
        intent.putExtra(EXTRA_I_START, iStart);
        startActivity(intent);
    }

    // ---- Network ----

    @Override
    public void onNetworkChanged(boolean hasNetwork) {
        if (hasNetwork) {
            if (networkBanner != null) {
                networkBanner.dismiss();
                networkBanner = null;
            }
        } else if (networkBanner == null) {
            View root = findViewById(android.R.id.content);
            networkBanner = Snackbar.make(root, R.string.no_network, Snackbar.LENGTH_INDEFINITE);
            networkBanner.show();
        }
    }
}
