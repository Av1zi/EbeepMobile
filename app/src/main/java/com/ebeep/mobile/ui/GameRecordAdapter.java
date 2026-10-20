package com.ebeep.mobile.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.GameRecord;
import com.ebeep.mobile.net.Protocol;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/** Shows one row for each finished game: the game, the result and the date. */
public class GameRecordAdapter extends RecyclerView.Adapter<GameRecordAdapter.RecordViewHolder> {

    private final ArrayList<GameRecord> records;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public GameRecordAdapter(ArrayList<GameRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        GameRecord record = records.get(position);
        Context context = holder.itemView.getContext();

        int gameName = record.getGame().equals(Protocol.TTT)
                ? R.string.game_tic_tac_toe : R.string.game_connect_four;
        holder.txtGame.setText(gameName);
        holder.txtDate.setText(dateFormat.format(new Date(record.getPlayedAt())));

        int resultText;
        int resultColor;
        if (record.getResult().equals(GameRecord.WIN)) {
            resultText = R.string.history_win;
            resultColor = R.color.result_win;
        } else if (record.getResult().equals(GameRecord.LOSS)) {
            resultText = R.string.history_loss;
            resultColor = R.color.result_loss;
        } else {
            resultText = R.string.history_draw;
            resultColor = R.color.result_draw;
        }
        holder.txtResult.setText(resultText);
        holder.txtResult.setTextColor(ContextCompat.getColor(context, resultColor));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        final TextView txtGame;
        final TextView txtResult;
        final TextView txtDate;

        RecordViewHolder(View itemView) {
            super(itemView);
            txtGame = itemView.findViewById(R.id.txtRecordGame);
            txtResult = itemView.findViewById(R.id.txtRecordResult);
            txtDate = itemView.findViewById(R.id.txtRecordDate);
        }
    }
}
