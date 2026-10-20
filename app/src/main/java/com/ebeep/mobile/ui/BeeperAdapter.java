package com.ebeep.mobile.ui;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.Beeper;

import java.util.ArrayList;

/** Fills the My Beepers list: one row per beeper with its last message. */
public class BeeperAdapter extends RecyclerView.Adapter<BeeperAdapter.BeeperViewHolder> {

    public interface OnBeeperClickListener {
        void onBeeperClick(Beeper beeper);

        void onBeeperLongClick(Beeper beeper);
    }

    private final ArrayList<Beeper> beepers;
    private final OnBeeperClickListener listener;

    public BeeperAdapter(ArrayList<Beeper> beepers, OnBeeperClickListener listener) {
        this.beepers = beepers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BeeperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beeper, parent, false);
        return new BeeperViewHolder(row);
    }

    /** "עכשיו" for the last minute, otherwise "לפני 5 דקות", "לפני שעה" and so on. */
    private CharSequence timeAgo(View view, long time) {
        if (System.currentTimeMillis() - time < DateUtils.MINUTE_IN_MILLIS) {
            return view.getContext().getString(R.string.beepers_just_now);
        }
        return DateUtils.getRelativeTimeSpanString(time);
    }

    @Override
    public void onBindViewHolder(@NonNull BeeperViewHolder holder, int position) {
        Beeper beeper = beepers.get(position);
        holder.txtNickname.setText(beeper.getNickname());

        if (beeper.getLastMessage() == null) {
            holder.txtLastMessage.setText(R.string.beepers_no_messages);
            holder.txtTime.setText("");
        } else {
            holder.txtLastMessage.setText(beeper.getLastMessage());
            holder.txtTime.setText(timeAgo(holder.itemView, beeper.getLastMessageTime()));
        }

        holder.itemView.setOnClickListener(v -> listener.onBeeperClick(beeper));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onBeeperLongClick(beeper);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return beepers.size();
    }

    /** Holds the views of one row, so they are not searched again for every bind. */
    static class BeeperViewHolder extends RecyclerView.ViewHolder {

        final TextView txtNickname;
        final TextView txtLastMessage;
        final TextView txtTime;

        BeeperViewHolder(View itemView) {
            super(itemView);
            txtNickname = itemView.findViewById(R.id.txtNickname);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}
