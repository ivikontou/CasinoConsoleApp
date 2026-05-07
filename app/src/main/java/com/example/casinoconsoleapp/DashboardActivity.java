package com.example.casinoconsoleapp;

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

            // Κλήση στο Mock Network (Αργότερα εδώ θα μπει το Thread με το Socket)
            List<Game> results = MockNetwork.sendSearch(selectedRisk, selectedLimit);
            adapter.setGames(results);
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
                // Αφαίρεση του ποσού και ενημέρωση UI
                currentBalance -= betAmount;
                updateBalanceUI();

                // Αποστολή στο δίκτυο
                MockNetwork.sendBet(game, betAmount);
                Toast.makeText(this, "Bet placed successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
