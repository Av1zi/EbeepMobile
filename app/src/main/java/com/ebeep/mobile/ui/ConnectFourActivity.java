package com.ebeep.mobile.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

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
    private ImageView imgMyColor;
    private TextView txtStatus;

    private long beeperRowId;
    private boolean resultSaved;
    private boolean askedForRematch;
    private AlertDialog resultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_four);
        SystemBars.apply(this, findViewById(R.id.root));
        setUpToolbar(R.string.game_connect_four, true);

        boardView = findViewById(R.id.boardView);
        imgMyColor = findViewById(R.id.imgMyColor);
        txtStatus = findViewById(R.id.txtStatus);
        beeperRowId = loadCurrentBeeper().getId();

        boardView.setOnColumnTapListener(this::onColumnTapped);
        setUpBackButton();
        startNewGame(getIntent().getBooleanExtra(EXTRA_I_START, false));
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

        // The side that starts is red, the other side is yellow
        int color = game.getMySymbol() == 'R' ? R.color.disc_red : R.color.disc_yellow;
        imgMyColor.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color)));
        updateBoard();
    }

    private void updateBoard() {
        boardView.invalidate();
        txtStatus.setText(statusText());

        if (game.isOver() && !resultSaved) {
            resultSaved = true;
            saveResult(resultForRecord());
            showResultDialog();
        }
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

    private void showResultDialog() {
        resultDialog = new AlertDialog.Builder(this)
                .setTitle(resultMessage())
                .setCancelable(false)
                .setPositiveButton(R.string.play_again, (dialog, which) -> {
                    askedForRematch = true;
                    BeeperConnection.get().sendToBeeper(Protocol.GAME_TOPIC_C4,
                            Protocol.C4 + Protocol.REMATCH, false);
                    updateBoard();
                })
                .setNegativeButton(R.string.play_back_to_games, (dialog, which) -> leaveGame())
                .show();
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
        if (resultDialog != null) {
            resultDialog.dismiss();
        }
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
        if (resultDialog != null) {
            resultDialog.dismiss();
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.play_beeper_left)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, which) -> finish())
                .show();
    }
}
