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

        // Koumpi Prosthikis Xrimatwn (KALEI TO POP-UP PLEON)
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
                    // SWSTO: Enimerwsi UI sto Main Thread
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

        final EditText input = new EditText(this);
        input.setHint("Enter Bet Amount (" + game.getMinBet() + " - " + game.getMaxBet() + ")");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("PLAY", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                double betAmount = Double.parseDouble(amountStr);

                // Topikos elegxos kanonwn
                if (betAmount > currentBalance) {
                    Toast.makeText(this, "Insufficient Balance!", Toast.LENGTH_LONG).show();
                } else if (betAmount < game.getMinBet() || betAmount > game.getMaxBet()) {
                    Toast.makeText(this, "Bet out of limits!", Toast.LENGTH_LONG).show();
                } else {
                    // Ftiaxnoume to minima me to onoma tou paikti
                    String betCommand = "PLAYER_CMD|BET|" + game.getGameName() + "|" + betAmount + "|" + playerName;

                    // Afairoume ta lefta amesws
                    currentBalance -= betAmount;
                    updateBalanceUI();

                    // Stelnoume to pontarisma ston Server
                    TcpClientManager.INSTANCE.placeBet(betCommand, new TcpClientManager.NetworkCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            runOnUiThread(() -> new AlertDialog.Builder(DashboardActivity.this)
                                    .setTitle("Apotelesma")
                                    .setMessage(result)
                                    .setPositiveButton("OK", null)
                                    .show());
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(DashboardActivity.this, "Sfalma: " + error, Toast.LENGTH_LONG).show();
                                // An epese o server, tou dinoume pisw ta lefta
                                currentBalance += betAmount;
                                updateBalanceUI();
                            });
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // NEA SYNARTISI: To Pop-up Prosthikis Xrimatwn
    private void showAddTokensDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Tokens");

        final EditText input = new EditText(this);
        input.setHint("Enter amount to add");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("ADD", (dialog, which) -> {
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
        builder.show();
    }

    private void updateBalanceUI() {
        tvBalance.setText("Current Balance: " + currentBalance + " FUN");
    }
}