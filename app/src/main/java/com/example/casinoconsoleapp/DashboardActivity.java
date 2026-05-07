package com.example.casinoconsoleapp;
import common.Game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvCurrentBalance;
    private Spinner spinnerRiskLevel, spinnerBetLimits;
    private RecyclerView recyclerViewGames;
    private GameAdapter adapter;

    // Τοπικό State για το Υπόλοιπο του Παίκτη
    private double currentBalance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Αρχικοποίηση Views
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        Button btnAddTokens = findViewById(R.id.btnAddTokens);
        Button btnSearchGames = findViewById(R.id.btnSearchGames);
        spinnerRiskLevel = findViewById(R.id.spinnerRiskLevel);
        spinnerBetLimits = findViewById(R.id.spinnerBetLimits);
        recyclerViewGames = findViewById(R.id.recyclerViewGames);

        updateBalanceUI();
        setupSpinners();
        setupRecyclerView();

        // Listeners
        btnAddTokens.setOnClickListener(v -> showAddTokensDialog());

        btnSearchGames.setOnClickListener(v -> {
            String selectedRisk = spinnerRiskLevel.getSelectedItem().toString();
            String selectedLimit = spinnerBetLimits.getSelectedItem().toString();

            // Φτιάχνουμε το String που περιμένει ο Master (προσάρμοσέ το αν ο Master περιμένει άλλη σύνταξη)
            String searchCommand = "PLAYER_CMD|SEARCH|" + selectedRisk + "|" + selectedLimit;

            Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();

            // ΚΛΗΣΗ ΣΤΟ ΠΡΑΓΜΑΤΙΚΟ ΔΙΚΤΥΟ (Μέλος Β)
            com.example.casinoconsoleapp.network.TcpClientManager.INSTANCE.searchGames(searchCommand, new com.example.casinoconsoleapp.network.TcpClientManager.NetworkCallback<List<common.Game>>() {
                @Override
                public void onSuccess(List<common.Game> result) {
                    // Αυτό τρέχει στο UI Thread χάρη στον Handler που έφτιαξε το Μέλος Β!
                    adapter.setGames(result);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(DashboardActivity.this, "Σφάλμα: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setupSpinners() {
        // Κατηγορίες Ρίσκου (όπως ορίζει η εκφώνηση)
        String[] risks = {"Low", "Medium", "High"};
        ArrayAdapter<String> riskAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, risks);
        spinnerRiskLevel.setAdapter(riskAdapter);

        // Όρια Πονταρίσματος
        String[] limits = {"$", "$$", "$$$"};
        ArrayAdapter<String> limitsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, limits);
        spinnerBetLimits.setAdapter(limitsAdapter);
    }

    private void setupRecyclerView() {
        recyclerViewGames.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameAdapter(this::showBettingDialog);
        recyclerViewGames.setAdapter(adapter);
    }

    private void updateBalanceUI() {
        tvCurrentBalance.setText("Current Balance: " + String.format("%.2f", currentBalance) + " FUN");
    }

    // Τοπική προσθήκη χρημάτων
    private void showAddTokensDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Tokens");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Amount to add");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                currentBalance += Double.parseDouble(amountStr);
                updateBalanceUI();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Διαχείριση Πονταρίσματος
    private void showBettingDialog(Game game) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Play " + game.name);
        builder.setMessage("Provider: " + game.provider + "\nRisk: " + game.riskLevel);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Bet Amount (FUN)");
        builder.setView(input);

        builder.setPositiveButton("PLAY", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double betAmount = Double.parseDouble(amountStr);

            // ΤΟΠΙΚΟΣ ΕΛΕΓΧΟΣ ΛΟΓΙΚΗΣ
            if (betAmount > currentBalance) {
                Toast.makeText(this, "Insufficient Balance!", Toast.LENGTH_LONG).show();
            } else {
                // Παίρνουμε το όνομα του παίκτη που μας ήρθε από την MainActivity
                String playerName = getIntent().getStringExtra("PLAYER_NAME");
                if (playerName == null) playerName = "UnknownPlayer";

                // Φτιάχνουμε την εντολή βάζοντας ΚΑΙ το όνομα του παίκτη
                String betCommand = "PLAYER_CMD|BET|" + game.getGameName() + "|" + betAmount + "|" + playerName;

                // Αφαίρεση του ποσού και ενημέρωση UI
                currentBalance -= betAmount;
                updateBalanceUI();

                // Αποστολή στο δίκτυο
                com.example.casinoconsoleapp.network.TcpClientManager.INSTANCE.placeBet(betCommand, new com.example.casinoconsoleapp.network.TcpClientManager.NetworkCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        // Όταν έρθει η απάντηση (π.χ. "Κέρδισες" ή "Έχασες"), βγάζουμε μήνυμα
                        new AlertDialog.Builder(DashboardActivity.this)
                                .setTitle("Αποτέλεσμα Πονταρίσματος")
                                .setMessage(result)
                                .setPositiveButton("OK", null)
                                .show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(DashboardActivity.this, "Σφάλμα: " + error, Toast.LENGTH_LONG).show();
                        // Προαιρετικά: Επιστροφή των χρημάτων στο balance αν έπεσε ο server
                        currentBalance += betAmount;
                        updateBalanceUI();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
