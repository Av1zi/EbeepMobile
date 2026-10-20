package com.ebeep.mobile.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ebeep.mobile.R;
import com.ebeep.mobile.net.Protocol;

/** Shows the two games the app can play, each with an invite button. */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    public interface OnInviteListener {
        void onInvite(String game);
    }

    // The list never changes, so it is written here and not in the database
    private static final String[] GAMES = {Protocol.TTT, Protocol.C4};
    private static final int[] NAMES = {R.string.game_tic_tac_toe, R.string.game_connect_four};
    private static final int[] RULES = {R.string.games_ttt_rules, R.string.games_c4_rules};

    private final OnInviteListener listener;

    public GameAdapter(OnInviteListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        holder.txtName.setText(NAMES[position]);
        holder.txtRules.setText(RULES[position]);
        holder.btnInvite.setOnClickListener(v -> listener.onInvite(GAMES[position]));
    }

    @Override
    public int getItemCount() {
        return GAMES.length;
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName;
        final TextView txtRules;
        final View btnInvite;

        GameViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtGameName);
            txtRules = itemView.findViewById(R.id.txtGameRules);
            btnInvite = itemView.findViewById(R.id.btnInvite);
        }
    }
}
