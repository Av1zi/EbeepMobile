package com.ebeep.mobile.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ebeep.mobile.R;
import com.ebeep.mobile.net.Protocol;

/** Shows the two games the app can play as a row of cards, like the beeper's game picker. */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    // The list never changes, so it is written here and not in the database
    static final String[] GAMES = {Protocol.TTT, Protocol.C4};
    private static final int[] NAMES = {R.string.game_tic_tac_toe, R.string.game_connect_four};
    private static final int[] RULES = {R.string.games_ttt_rules, R.string.games_c4_rules};
    private static final int[] ART = {R.drawable.ic_play_tictactoe_art, R.drawable.ic_play_connect4_art};

    // A card takes three quarters of the screen, so the next card peeks in from the side
    private static final float CARD_WIDTH_PART = 0.75f;

    /** The width of one card in pixels. */
    static int cardWidth(Context context) {
        return (int) (context.getResources().getDisplayMetrics().widthPixels * CARD_WIDTH_PART);
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        view.getLayoutParams().width = cardWidth(parent.getContext());
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        holder.imgArt.setImageResource(ART[position]);
        holder.txtName.setText(NAMES[position]);
        holder.txtRules.setText(RULES[position]);
    }

    @Override
    public int getItemCount() {
        return GAMES.length;
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgArt;
        final TextView txtName;
        final TextView txtRules;

        GameViewHolder(View itemView) {
            super(itemView);
            imgArt = itemView.findViewById(R.id.imgGameArt);
            txtName = itemView.findViewById(R.id.txtGameName);
            txtRules = itemView.findViewById(R.id.txtGameRules);
        }
    }
}
