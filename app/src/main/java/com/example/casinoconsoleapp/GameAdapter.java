package com.example.casinoconsoleapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import common.Game;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> games;
    private final OnGameClickListener listener;

    public interface OnGameClickListener {
        void onGameClick(Game game);
    }

    public GameAdapter(List<Game> games, OnGameClickListener listener) {
        this.games = games;
        this.listener = listener;
    }

    public void setGames(List<Game> newGames) {
        this.games = newGames;
        notifyDataSetChanged(); // Ενημερώνει τη λίστα στην οθόνη
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.tvName.setText(game.getGameName());
        holder.tvProvider.setText(game.getProviderName());
        holder.tvRisk.setText("Risk: " + game.getRiskLevel());

        // Όταν πατάει πάνω στο παιχνίδι
        holder.itemView.setOnClickListener(v -> listener.onGameClick(game));
    }

    @Override
    public int getItemCount() {
        return games == null ? 0 : games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvProvider, tvRisk;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGameName);
            tvProvider = itemView.findViewById(R.id.tvProvider);
            tvRisk = itemView.findViewById(R.id.tvRisk);
        }
    }
}