package com.example.casinoconsoleapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import common.Game;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> games;
    private OnItemClickListener listener;

    // Interface gia na piasoume to click se ena paixnidi
    public interface OnItemClickListener {
        void onItemClick(Game game);
    }

    // Constructor tou Adapter
    public GameAdapter(List<Game> games, OnItemClickListener listener) {
        this.games = games;
        this.listener = listener;
    }

    // Methodos gia na ananewnoume th lista me nea dedomena (px meta to search)
    public void setGames(List<Game> games) {
        this.games = games;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Fortwnoume to layout tou kathe antikeimenou (item_game.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        // Pairnoume to paixnidi sth sugkekrimeni thesi ths listas
        Game game = games.get(position);

        // Gemizoume ta TextViews me ta stoixeia tou paixnidiou
        holder.tvGameName.setText(game.getGameName());
        holder.tvProvider.setText(game.getProviderName());
        holder.tvRisk.setText("Risk: " + game.getRiskLevel());

        // --- I LOGIKI TIS EIKONAS ---
        String base64Image = game.getGameLogo();

        if (base64Image != null && !base64Image.isEmpty()) {
            // 1. Vazoume ti varia douleia (metatropi eikonas) se neo Background Thread gia na min kollaei to UI
            new Thread(() -> {
                try {
                    // Diavazoume to byte array apo to base64 string
                    byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                    // Metatrepoume ta bytes se Bitmap
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    // 2. Epistrefoume sto Main (UI) Thread MONO gia na emfanisoume tin eikona sto ImageView
                    holder.itemView.post(() -> {
                        holder.imgGameLogo.setImageBitmap(bmp);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    // An xalasei i metatropi (px lathos string i corrupt data), vale tin proepilegmeni eikona
                    holder.itemView.post(() -> {
                        holder.imgGameLogo.setImageResource(R.mipmap.ic_launcher);
                    });
                }
            }).start();
        } else {
            // An to paixnidi den exei katholou logo sto JSON (einai keno string), vazoume kateytheian to default
            holder.imgGameLogo.setImageResource(R.mipmap.ic_launcher);
        }

        // Bazoume ton listener sto click gia na anoigei to popup tou pontarismatos
        holder.itemView.setOnClickListener(v -> listener.onItemClick(game));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    // Eswteriki klasi gia na kratame ta Views kathe stoixeiou
    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView tvGameName, tvProvider, tvRisk;
        ImageView imgGameLogo; // To ImageView gia to logotypo

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            // Syndesh twn metavlitwn me ta id apo to item_game.xml
            tvGameName = itemView.findViewById(R.id.tvGameName);
            tvProvider = itemView.findViewById(R.id.tvProvider);
            tvRisk = itemView.findViewById(R.id.tvRisk);
            imgGameLogo = itemView.findViewById(R.id.imgGameLogo);
        }
    }
}