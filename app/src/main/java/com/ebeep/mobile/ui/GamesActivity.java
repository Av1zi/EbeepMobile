package com.ebeep.mobile.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.Beeper;
import com.ebeep.mobile.net.BeeperConnection;
import com.ebeep.mobile.net.Protocol;
import com.google.android.material.button.MaterialButton;

/** Screen 8: choose a game and invite the beeper. Waits up to a minute for its answer. */
public class GamesActivity extends BaseActivity {

    private static final long WAIT_MILLIS = 60_000;

    private Beeper beeper;

    private RecyclerView listGames;
    private LinearLayoutManager layoutManager;
    private PagerSnapHelper snapHelper;
    private View[] dots;
    // The game that is in the middle of the carousel
    private int currentGame = 0;

    // The game we invited the beeper to, or null when we are not waiting
    private String waitingForGame;
    private AlertDialog waitingDialog;
    private CountDownTimer inviteTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);
        SystemBars.apply(this, findViewById(R.id.root));
        setUpToolbar(R.string.games_title, true);

        beeper = loadCurrentBeeper();

        setUpCarousel();
        setUpSoftKeys();
        showDots();
    }

    /** A row of cards that snaps to one game at a time. */
    private void setUpCarousel() {
        listGames = findViewById(R.id.listGames);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        listGames.setLayoutManager(layoutManager);
        listGames.setAdapter(new GameAdapter());

        // Padding on both sides lets the first and the last card stop in the middle
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int sidePadding = (screenWidth - GameAdapter.cardWidth(this)) / 2;
        listGames.setPadding(sidePadding, 0, sidePadding, 0);

        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(listGames);
        dots = new View[]{findViewById(R.id.dotFirst), findViewById(R.id.dotSecond)};

        listGames.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                View snapView = snapHelper.findSnapView(layoutManager);
                if (snapView != null) {
                    currentGame = layoutManager.getPosition(snapView);
                    showDots();
                }
            }
        });
    }

    private void setUpSoftKeys() {
        setUpSoftKey(R.id.softKeyStart, R.string.games_previous, v -> scrollToGame(currentGame - 1));
        setUpSoftKey(R.id.softKeyCenter, R.string.games_play, v -> sendInvitation(GameAdapter.GAMES[currentGame]));
        setUpSoftKey(R.id.softKeyEnd, R.string.games_next, v -> scrollToGame(currentGame + 1));

        ((MaterialButton) findViewById(R.id.softKeyStart)).setIconResource(R.drawable.ic_chevron_back);
        ((MaterialButton) findViewById(R.id.softKeyEnd)).setIconResource(R.drawable.ic_chevron_next);
        ((MaterialButton) findViewById(R.id.softKeyEnd)).setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_END);
    }

    private void scrollToGame(int position) {
        if (position >= 0 && position < GameAdapter.GAMES.length) {
            listGames.smoothScrollToPosition(position);
        }
    }

    /** The dot of the selected game is filled, the other one is only an outline. */
    private void showDots() {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(i == currentGame ? R.drawable.ic_play_dot_on : R.drawable.ic_play_dot_off);
        }
    }

    private void sendInvitation(String game) {
        if (!BeeperConnection.get().isConnected()) {
            Toast.makeText(this, R.string.games_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        BeeperConnection.get().sendToBeeper(Protocol.GAMES, game + Protocol.START, false);
        startWaiting(game);
    }

    private void startWaiting(String game) {
        waitingForGame = game;
        waitingDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.games_waiting_title)
                .setView(R.layout.dialog_waiting)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, (dialog, which) -> cancelInvitation())
                .show();

        inviteTimer = new CountDownTimer(WAIT_MILLIS, 1_000) {
            @Override
            public void onTick(long millisLeft) {
            }

            @Override
            public void onFinish() {
                cancelInvitation();
                Toast.makeText(GamesActivity.this, R.string.games_no_answer, Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    /** Tells the beeper we are no longer waiting, then closes the waiting dialog. */
    private void cancelInvitation() {
        if (waitingForGame == null) {
            return;
        }
        BeeperConnection.get().sendToBeeper(Protocol.GAMES, waitingForGame + Protocol.LEFT, false);
        stopWaiting();
    }

    private void stopWaiting() {
        waitingForGame = null;
        if (inviteTimer != null) {
            inviteTimer.cancel();
        }
        if (waitingDialog != null) {
            waitingDialog.dismiss();
        }
    }

    @Override
    public void onBeeperMessage(String subTopic, String text) {
        if (waitingForGame != null && subTopic.equals(Protocol.GAMES)) {
            String game = waitingForGame;

            if (text.equals(game + Protocol.START_ACK)) {
                stopWaiting();
                openGame(game, true);
                return;
            }
            if (text.equals(game + Protocol.START)) {
                // Both of us invited at once. No super: it would open the invitation dialog
                joinIfBeeperWinsTie(game);
                return;
            }
            if (text.equals(game + Protocol.LEFT)) {
                stopWaiting();
                Toast.makeText(this, R.string.games_declined, Toast.LENGTH_LONG).show();
                return;
            }
        }
        super.onBeeperMessage(subTopic, text);
    }

    /** If the phone wins the tie-break it keeps waiting, otherwise it joins the beeper's game. */
    private void joinIfBeeperWinsTie(String game) {
        if (Protocol.phoneStartsOnTie(beeper.getPhoneId(), beeper.getBeeperId())) {
            return;
        }
        stopWaiting();
        BeeperConnection.get().sendToBeeper(Protocol.GAMES, game + Protocol.START_ACK, false);
        openGame(game, false);
    }

    @Override
    protected void onDestroy() {
        if (inviteTimer != null) {
            inviteTimer.cancel();
        }
        if (waitingDialog != null) {
            waitingDialog.dismiss();
        }
        super.onDestroy();
    }
}
