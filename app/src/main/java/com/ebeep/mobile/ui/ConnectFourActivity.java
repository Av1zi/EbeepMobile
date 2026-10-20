package com.ebeep.mobile.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.GameRecord;
import com.ebeep.mobile.game.BoardGame;
import com.ebeep.mobile.game.ConnectFourGame;
import com.ebeep.mobile.net.BeeperConnection;
import com.ebeep.mobile.net.Protocol;

/** Screen 11: Connect 4 against the beeper. */
public class ConnectFourActivity extends BaseActivity {

    private ConnectFourGame game;
    private ConnectFourBoardView boardView;
    private TextView txtStatus;
    private TextView txtMyColor;

    private long beeperRowId;
    private boolean resultSaved;
    private boolean askedForRematch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_four);
        SystemBars.apply(this, findViewById(R.id.root));
        setUpToolbar(R.string.game_connect_four, true);

        boardView = findViewById(R.id.boardView);
        txtStatus = findViewById(R.id.txtStatus);
        txtMyColor = findViewById(R.id.txtMyColor);
        beeperRowId = loadCurrentBeeper().getId();

        boardView.setOnColumnTapListener(this::onColumnTapped);
        setUpBackButton();
        setUpSoftKeys();
        startNewGame(getIntent().getBooleanExtra(EXTRA_I_START, false));
    }

    /** Exit on the left, and "rematch" in the middle once the game is over. */
    private void setUpSoftKeys() {
        setUpSoftKey(R.id.softKeyStart, R.string.play_exit, v -> getOnBackPressedDispatcher().onBackPressed());
        setUpSoftKey(R.id.softKeyCenter, R.string.play_rematch, v -> askForRematch());
        hideSoftKey(R.id.softKeyEnd);
    }

    private void onColumnTapped(int col) {
        if (game.playMine(col)) {
            BeeperConnection.get().sendToBeeper(Protocol.GAME_TOPIC_C4,
                    ConnectFourGame.moveToMessage(col), false);
            updateBoard();
        }
    }

    private void startNewGame(boolean iStart) {
        game = new ConnectFourGame(iStart);
        resultSaved = false;
        askedForRematch = false;
        boardView.setGame(game);

        // The side that starts is black, the other side is white
        boolean iAmBlack = game.getMySymbol() == 'R';
        txtMyColor.setText(iAmBlack ? R.string.play_you_are_black : R.string.play_you_are_white);
        // The sample disc goes after the text (on the left in Hebrew)
        txtMyColor.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                iAmBlack ? R.drawable.ic_play_disc_black : R.drawable.ic_play_disc_white, 0);
        updateBoard();
    }

    private void updateBoard() {
        boardView.invalidate();
        txtStatus.setText(statusText());

        if (game.isOver() && !resultSaved) {
            resultSaved = true;
            saveResult(resultForRecord());
        }
        // The rematch key only shows when the game is over and we haven't asked yet
        boolean canAskForRematch = game.isOver() && !askedForRematch;
        findViewById(R.id.softKeyCenter).setVisibility(canAskForRematch ? View.VISIBLE : View.INVISIBLE);
    }

    private int statusText() {
        if (askedForRematch) {
            return R.string.play_waiting_rematch;
        }
        if (game.isOver()) {
            return resultMessage();
        }
        return game.isMyTurn() ? R.string.play_your_turn : R.string.play_beeper_turn;
    }

    private int resultMessage() {
        if (game.getResult() == BoardGame.WIN) {
            return R.string.play_won;
        }
        return game.getResult() == BoardGame.LOSS ? R.string.play_lost : R.string.play_draw;
    }

    private String resultForRecord() {
        if (game.getResult() == BoardGame.WIN) {
            return GameRecord.WIN;
        }
        return game.getResult() == BoardGame.LOSS ? GameRecord.LOSS : GameRecord.DRAW;
    }

    private void saveResult(String result) {
        DatabaseHelper database = new DatabaseHelper(this);
        database.addGameRecord(beeperRowId, Protocol.C4, result);
        database.close();
    }

    private void askForRematch() {
        askedForRematch = true;
        BeeperConnection.get().sendToBeeper(Protocol.GAME_TOPIC_C4,
                Protocol.C4 + Protocol.REMATCH, false);
        updateBoard();
    }

    private void leaveGame() {
        BeeperConnection.get().sendToBeeper(Protocol.GAMES, Protocol.C4 + Protocol.LEFT, false);
        finish();
    }

    private void setUpBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (game.isOver()) {
                    leaveGame();
                } else {
                    askToQuit();
                }
            }
        });
    }

    private void askToQuit() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.play_quit_title)
                .setMessage(R.string.play_quit_message)
                .setPositiveButton(R.string.play_quit_yes, (dialog, which) -> {
                    saveResult(GameRecord.LOSS);
                    leaveGame();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onBeeperMessage(String subTopic, String text) {
        super.onBeeperMessage(subTopic, text);

        if (subTopic.equals(Protocol.GAME_TOPIC_C4)) {
            if (text.equals(Protocol.C4 + Protocol.REMATCH_ACK)) {
                startNewGame(true);
            } else if (text.equals(Protocol.C4 + Protocol.REMATCH)) {
                onRematchRequested();
            } else if (game.playOpponent(text)) {
                updateBoard();
            }
        } else if (subTopic.equals(Protocol.GAMES) && text.equals(Protocol.C4 + Protocol.LEFT)) {
            showBeeperLeftDialog();
        }
    }

    private void onRematchRequested() {
        if (askedForRematch) {
            // Both asked at once: the same tie-break as for invitations
            boolean iStart = Protocol.phoneStartsOnTie(
                    BeeperConnection.get().getBeeper().getPhoneId(),
                    BeeperConnection.get().getBeeper().getBeeperId());
            startNewGame(iStart);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.play_rematch_title)
                .setMessage(R.string.play_rematch_message)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    BeeperConnection.get().sendToBeeper(Protocol.GAME_TOPIC_C4,
                            Protocol.C4 + Protocol.REMATCH_ACK, false);
                    startNewGame(false);
                })
                .setNegativeButton(R.string.no, (dialog, which) -> leaveGame())
                .show();
    }

    private void showBeeperLeftDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.play_beeper_left)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, which) -> finish())
                .show();
    }
}
