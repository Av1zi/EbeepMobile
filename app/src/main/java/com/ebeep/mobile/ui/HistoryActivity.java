package com.ebeep.mobile.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.data.GameRecord;
import com.ebeep.mobile.net.Protocol;

import java.util.ArrayList;
import java.util.HashMap;

/** Screen 12: wins, losses and draws for each game, and the list of all games played. */
public class HistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        SystemBars.apply(this, findViewById(R.id.root));
        setUpToolbar(R.string.history_title, true);

        long beeperRowId = loadCurrentBeeper().getId();

        DatabaseHelper database = new DatabaseHelper(this);
        HashMap<String, int[]> summary = database.getGameSummary(beeperRowId);
        ArrayList<GameRecord> records = database.getGameRecords(beeperRowId);
        database.close();

        showSummary(R.id.tileTtt, R.string.game_tic_tac_toe, summary.get(Protocol.TTT));
        showSummary(R.id.tileC4, R.string.game_connect_four, summary.get(Protocol.C4));
        showRecords(records);
    }

    /** counts is {wins, losses, draws}, or null if the game was never played. */
    private void showSummary(int tileId, int gameName, int[] counts) {
        if (counts == null) {
            counts = new int[3];
        }
        View tile = findViewById(tileId);
        ((TextView) tile.findViewById(R.id.txtSummaryName)).setText(gameName);
        ((TextView) tile.findViewById(R.id.txtWins)).setText(String.valueOf(counts[0]));
        ((TextView) tile.findViewById(R.id.txtLosses)).setText(String.valueOf(counts[1]));
        ((TextView) tile.findViewById(R.id.txtDraws)).setText(String.valueOf(counts[2]));
    }

    private void showRecords(ArrayList<GameRecord> records) {
        RecyclerView listRecords = findViewById(R.id.listRecords);
        listRecords.setLayoutManager(new LinearLayoutManager(this));
        listRecords.setAdapter(new GameRecordAdapter(records));

        findViewById(R.id.layoutEmpty).setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
