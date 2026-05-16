package com.example.casinoconsoleapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import common.Game;
import com.example.casinoconsoleapp.network.TcpClientManager;

public class DashboardActivity extends AppCompatActivity {

    private double currentBalance = 0.0;
    private TextView tvBalance;
    private GameAdapter adapter;
    private String playerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Pairnoume to onoma apo to login
        playerName = getIntent().getStringExtra("PLAYER_NAME");
        if (playerName == null) playerName = "Player";
        Toast.makeText(this, "Welcome " + playerName, Toast.LENGTH_SHORT).show();

        // Syndesi me to XML
        tvBalance = findViewById(R.id.tvCurrentBalance);
        Button btnAddTokens = findViewById(R.id.btnAddTokens);
        Spinner spinnerRisk = findViewById(R.id.spinnerRiskLevel);
        Spinner spinnerCategory = findViewById(R.id.spinnerBetLimits);
        Button btnSearch = findViewById(R.id.btnSearchGames);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewGames);

        // Rythmisi twn Dropdowns (Spinners)
        String[] riskLevels = {"ANY", "low", "medium", "high"};
        spinnerRisk.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, riskLevels));

        String[] categories = {"ANY", "$", "$$", "$$$"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        // Rythmisi tis Listas (RecyclerView)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameAdapter(new ArrayList<>(), game -> showBettingDialog(game));
        recyclerView.setAdapter(adapter);

        // Koumpi Prosthikis Xrimatwn
        btnAddTokens.setOnClickListener(v -> showAddTokensDialog());

        // Koumpi Anazitisis (Kalei to Diktio)
        btnSearch.setOnClickListener(v -> {
            String selectedRisk = spinnerRisk.getSelectedItem().toString();
            String selectedLimit = spinnerCategory.getSelectedItem().toString();
            String searchCommand = "PLAYER_CMD|SEARCH|" + selectedRisk + "|" + selectedLimit + "|ANY|0";
            Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();

            TcpClientManager.INSTANCE.searchGames(searchCommand, new TcpClientManager.NetworkCallback<List<Game>>() {
                @Override
                public void onSuccess(List<Game> result) {
                    runOnUiThread(() -> adapter.setGames(result));
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Sfalma: " + error, Toast.LENGTH_LONG).show());
                }
            });
        });
    }

    // To Pop-up tou Pontarismatos
    private void showBettingDialog(Game game) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Play " + game.getGameName());

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(60, 40, 60, 40);
        container.setBackgroundColor(android.graphics.Color.parseColor("#cdd3ec"));

        final EditText input = new EditText(this);
        input.setHint("Enter Bet Amount (" + game.getMinBet() + " - " + game.getMaxBet() + ")");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setTextColor(android.graphics.Color.BLACK);
        input.setHintTextColor(android.graphics.Color.DKGRAY);

        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("PLAY", null);
        builder.setNegativeButton("Cancel", null);

        // Kataskeyi kai emfanisi tou dialogou gia na paroume to window
        AlertDialog dialog = builder.create();
        dialog.show();

        // ALLAGI: Kanoume OLO to parathyro mple gia na fygoun ta leyka borders panw-katw
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#cdd3ec")));
        }
        // Kleidwnoume skouro xrwma sta koympia gia na fainovtai sto mple
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.BLACK);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                double betAmount = Double.parseDouble(amountStr);

                if (betAmount > currentBalance) {
                    Toast.makeText(this, "Insufficient Balance!", Toast.LENGTH_LONG).show();
                } else if (betAmount < game.getMinBet() || betAmount > game.getMaxBet()) {
                    Toast.makeText(this, "Bet out of limits!", Toast.LENGTH_LONG).show();
                } else {
                    String betCommand = "PLAYER_CMD|BET|" + game.getGameName() + "|" + betAmount + "|" + playerName;

                    currentBalance -= betAmount;
                    updateBalanceUI();
                    dialog.dismiss(); // Kleinoume to input pop-up

                    TcpClientManager.INSTANCE.placeBet(betCommand, new TcpClientManager.NetworkCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            runOnUiThread(() -> {
                                android.widget.LinearLayout customLayout = new android.widget.LinearLayout(DashboardActivity.this);
                                customLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
                                customLayout.setPadding(60, 60, 60, 60);
                                customLayout.setGravity(android.view.Gravity.CENTER);

                                // 1. Orizουμε μια μεταβλητη για το xrwμα όλου του παραθύρου
                                String windowColor = "#cdd3ec";

                                TextView tvEmoji = new TextView(DashboardActivity.this);
                                tvEmoji.setTextSize(64);
                                tvEmoji.setGravity(android.view.Gravity.CENTER);
                                tvEmoji.setPadding(0, 0, 0, 20);

                                TextView tvMessage = new TextView(DashboardActivity.this);
                                tvMessage.setText(result);
                                tvMessage.setTextSize(22);
                                tvMessage.setGravity(android.view.Gravity.CENTER);
                                tvMessage.setTypeface(null, android.graphics.Typeface.BOLD);

                                String dialogTitle = "Apotelesma";
                                String lowerResult = result.toLowerCase();

                                // --- ELEGXOS KAI XROMATA ---
                                if (lowerResult.contains("win") || lowerResult.contains("won") || lowerResult.contains("kerd") || lowerResult.contains("κερδ")) {
                                    tvEmoji.setText("🎉 💰");
                                    tvMessage.setTextColor(android.graphics.Color.parseColor("#1B5E20"));
                                    customLayout.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
                                    dialogTitle = "Money! Money! Money!";
                                    windowColor = "#E8F5E9"; // 2. EDW: Olo to parathyro ginetai prasino
                                } else if (lowerResult.contains("lose") || lowerResult.contains("lost") || lowerResult.contains("xas") || lowerResult.contains("χασ")) {
                                    tvEmoji.setText("📉 😞");
                                    tvMessage.setTextColor(android.graphics.Color.parseColor("#B71C1C"));
                                    customLayout.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
                                    dialogTitle = "No Money! No Money! No Money!";
                                    windowColor = "#FFEBEE"; // 3. EDW: Olo to parathyro ginetai kokkino
                                } else {
                                    tvEmoji.setText("ℹ️");
                                    tvMessage.setTextColor(android.graphics.Color.BLACK);
                                    customLayout.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
                                    windowColor = "#F5F5F5";
                                }

                                customLayout.addView(tvEmoji);
                                customLayout.addView(tvMessage);

                                AlertDialog.Builder resBuilder = new AlertDialog.Builder(DashboardActivity.this);
                                resBuilder.setTitle(dialogTitle);
                                resBuilder.setView(customLayout);
                                resBuilder.setPositiveButton("OK", null);

                                AlertDialog resDialog = resBuilder.create();
                                resDialog.show();

                                // 4. EDW: To parathyro pairnei dynamically to swsto xrwma (Prasino i Kokkino)
                                if (resDialog.getWindow() != null) {
                                    resDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor(windowColor)));
                                }
                                resDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.BLACK);
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(DashboardActivity.this, "Sfalma: " + error, Toast.LENGTH_LONG).show();
                                currentBalance += betAmount;
                                updateBalanceUI();
                            });
                        }
                    });
                }
            }
        });
    }

    // To Pop-up Prosthikis Xrimatwn
    private void showAddTokensDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Tokens");

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(60, 40, 60, 40);
        container.setBackgroundColor(android.graphics.Color.parseColor("#cdd3ec"));

        final EditText input = new EditText(this);
        input.setHint("Enter amount to add");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setTextColor(android.graphics.Color.BLACK);
        input.setHintTextColor(android.graphics.Color.DKGRAY);

        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("ADD", (dialogInterface, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                double amountToAdd = Double.parseDouble(amountStr);
                if (amountToAdd > 0) {
                    currentBalance += amountToAdd;
                    updateBalanceUI();
                    Toast.makeText(this, "Added " + amountToAdd + " FUN!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please enter a valid amount!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // ALLAGI: OLO to parathyro twn tokens ginetai pleon katamaplo apo akri se akri
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#cdd3ec")));
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.BLACK);
    }

    private void updateBalanceUI() {
        tvBalance.setText("Current Balance: " + currentBalance + " FUN");
    }
}