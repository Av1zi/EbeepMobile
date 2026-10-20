package com.ebeep.mobile.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.GameRecord;
import com.ebeep.mobile.game.BoardGame;
import com.ebeep.mobile.game.TicTacToeGame;
import com.ebeep.mobile.net.BeeperConnection;
import com.ebeep.mobile.net.Protocol;
import com.google.android.material.button.MaterialButton;

/** Screen 10: tic-tac-toe against the beeper. */
public class TicTacToeActivity extends BaseActivity {

    private TicTacToeGame game;
    private final MaterialButton[][] cells = new MaterialButton[TicTacToeGame.SIZE][TicTacToeGame.SIZE];

    private TextView txtSymbol;
    private TextView txtStatus;

    private long beeperRowId;
    private boolean resultSaved;
    private boolean askedForRematch;
    private AlertDialog resultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);
        SystemBars.apply(this, findViewById(R.id.root));
        setUpToolbar(R.string.game_tic_tac_toe, true);

        txtSymbol = findViewById(R.id.txtSymbol);
        txtStatus = findViewById(R.id.txtStatus);
        beeperRowId = loadCurrentBeeper().getId();

        setUpCells();
        setUpBackButton();
        startNewGame(getIntent().getBooleanExtra(EXTRA_I_START, false));
    }

    /** Every button has the tag "row,col", so one listener serves all nine. */
    private void setUpCells() {
        GridLayout grid = findViewById(R.id.gridBoard);
        for (int i = 0; i < grid.getChildCount(); i++) {
            MaterialButton cell = (MaterialButton) grid.getChildAt(i);
            String[] parts = cell.getTag().toString().split(",");
            cells[Integer.parseInt(parts[0])][Integer.parseInt(parts[1])] = cell;
            cell.setOnClickListener(this::onCellClicked);
        }
    }

    private void onCellClicked(View cell) {
        String[] parts = cell.getTag().toString().split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        if (game.playMine(row, col)) {
            BeeperConnection.get().sendToBeeper(Protocol.GAME_TOPIC_TTT,
                    TicTacToeGame.moveToMessage(row, col), false);
            updateBoard();
        }
    }

    private void startNewGame(boolean iStart) {
        game = new TicTacToeGame(iStart);
        resultSaved = false;
        askedForRematch = false;
        txtSymbol.setText(getString(R.string.play_you_are, String.valueOf(game.getMySymbol())));
        updateBoard();
    }

    private void updateBoard() {
        for (int row = 0; row < TicTacToeGame.SIZE; row++) {
            for (int col = 0; col < TicTacToeGame.SIZE; col++) {
                char symbol = game.getCell(row, col);
                cells[row][col].setText(symbol == BoardGame.EMPTY ? "" : String.valueOf(symbol));
                cells[row][col].setEnabled(game.isMyTurn() && symbol == BoardGame.EMPTY);
            }
        }
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
        database.addGameRecord(beeperRowId, Protocol.TTT, result);
        database.close();
    }

    private void showResultDialog() {
        resultDialog = new AlertDialog.Builder(this)
                .setTitle(resultMessage())
                .setCancelable(false)
                .setPositiveButton(R.string.play_again, (dialog, which) -> {
                    askedForRematch = true;
                    BeeperConnection.get().sendToBeeper(Protocol.GAME_TOPIC_TTT,
                            Protocol.TTT + Protocol.REMATCH, false);
                    updateBoard();
                })
                .setNegativeButton(R.string.play_back_to_games, (dialog, which) -> leaveGame())
                .show();
    }

    private void leaveGame() {
        BeeperConnection.get().sendToBeeper(Protocol.GAMES, Protocol.TTT + Protocol.LEFT, false);
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

        if (subTopic.equals(Protocol.GAME_TOPIC_TTT)) {
            if (text.equals(Protocol.TTT + Protocol.REMATCH_ACK)) {
                startNewGame(true);
            } else if (text.equals(Protocol.TTT + Protocol.REMATCH)) {
                onRematchRequested();
            } else if (game.playOpponent(text)) {
                updateBoard();
            }
        } else if (subTopic.equals(Protocol.GAMES) && text.equals(Protocol.TTT + Protocol.LEFT)) {
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
                    BeeperConnection.get().sendToBeeper(Protocol.GAME_TOPIC_TTT,
                            Protocol.TTT + Protocol.REMATCH_ACK, false);
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
