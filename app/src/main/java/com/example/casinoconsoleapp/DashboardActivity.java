package com.example.casinoconsoleapp;

import android.os.Bundle;
import android.view.View; // ΑΥΤΟ ΕΛΕΙΠΕ!
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.view.Gravity;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

        // --- ΕΝΑΡΞΗ ΠΡΟΣΘΗΚΗΣ ΓΙΑ ΤΟ PADDING ---
        int paddingDp = 16;
        float density = getResources().getDisplayMetrics().density;
        int paddingPx = (int) (paddingDp * density);

        // ΠΡΟΣΟΧΗ: Το root layout στο activity_dashboard.xml πρέπει να έχει android:id="@+id/dashboard_root"
        View rootLayout = findViewById(R.id.dashboard_root);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        systemBars.left + paddingPx,
                        systemBars.top,
                        systemBars.right + paddingPx,
                        systemBars.bottom
                );
                return insets;
            });
        }
        // --- ΤΕΛΟΣ ΠΡΟΣΘΗΚΗΣ ΓΙΑ ΤΟ PADDING ---


        if (rootLayout instanceof LinearLayout) {
            TextView titleTextView = new TextView(this);
            titleTextView.setText("Casino Console App");
            titleTextView.setTextSize(28); // Μέγεθος
            titleTextView.setTypeface(null, Typeface.BOLD); // Έντονα
            titleTextView.setGravity(Gravity.CENTER_HORIZONTAL); // Κεντράρισμα
            titleTextView.setPadding(0, 20, 0, 40); // Περιθώριο

            // Προσθήκη στην κορυφή (index 0)
            ((LinearLayout) rootLayout).addView(titleTextView, 0);
        }



        // Παίρνουμε το όνομα από το login
        playerName = getIntent().getStringExtra("PLAYER_NAME");
        if (playerName == null) playerName = "Player";
        Toast.makeText(this, "Welcome " + playerName, Toast.LENGTH_SHORT).show();

        // Σύνδεση με το XML
        tvBalance = findViewById(R.id.tvCurrentBalance);
        Button btnAddTokens = findViewById(R.id.btnAddTokens);
        Spinner spinnerRisk = findViewById(R.id.spinnerRiskLevel);
        Spinner spinnerCategory = findViewById(R.id.spinnerBetLimits);
        Button btnSearch = findViewById(R.id.btnSearchGames);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewGames);

        // Ρύθμιση των Dropdowns (Spinners)
        String[] riskLevels = {"Any", "low", "medium", "high"};
        spinnerRisk.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, riskLevels));

        String[] categories = {"Any", "$", "$$", "$$$"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        // Ρύθμιση της Λίστας (RecyclerView)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameAdapter(new ArrayList<>(), this::showBettingDialog);
        recyclerView.setAdapter(adapter);

        // Κουμπί Προσθήκης Χρημάτων
        btnAddTokens.setOnClickListener(v -> {
            currentBalance += 100.0;
            updateBalanceUI();
            Toast.makeText(this, "Added 100 FUN!", Toast.LENGTH_SHORT).show();
        });

        // Κουμπί Αναζήτησης (Καλεί το Δίκτυο)
        btnSearch.setOnClickListener(v -> {
            String selectedRisk = spinnerRisk.getSelectedItem().toString();
            String selectedLimit = spinnerCategory.getSelectedItem().toString();

            String searchCommand = "PLAYER_CMD|SEARCH|" + selectedRisk + "|" + selectedLimit;
            Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();

            TcpClientManager.INSTANCE.searchGames(searchCommand, new TcpClientManager.NetworkCallback<List<Game>>() {
                @Override
                public void onSuccess(List<Game> result) {
                    runOnUiThread(() -> adapter.setGames(result));
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Σφάλμα: " + error, Toast.LENGTH_LONG).show());
                }
            });
        });
    }

    // Το Pop-up του Πονταρίσματος
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

                // Τοπικός έλεγχος κανόνων
                if (betAmount > currentBalance) {
                    Toast.makeText(this, "Insufficient Balance!", Toast.LENGTH_LONG).show();
                } else if (betAmount < game.getMinBet() || betAmount > game.getMaxBet()) {
                    Toast.makeText(this, "Bet out of limits!", Toast.LENGTH_LONG).show();
                } else {
                    // Φτιάχνουμε το μήνυμα με το όνομα του παίκτη
                    String betCommand = "PLAYER_CMD|BET|" + game.getGameName() + "|" + betAmount + "|" + playerName;

                    // Αφαιρούμε τα λεφτά αμέσως
                    currentBalance -= betAmount;
                    updateBalanceUI();

                    // Στέλνουμε το ποντάρισμα στον Server
                    TcpClientManager.INSTANCE.placeBet(betCommand, new TcpClientManager.NetworkCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            runOnUiThread(() -> new AlertDialog.Builder(DashboardActivity.this)
                                    .setTitle("Αποτέλεσμα")
                                    .setMessage(result)
                                    .setPositiveButton("OK", null)
                                    .show());
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(DashboardActivity.this, "Σφάλμα: " + error, Toast.LENGTH_LONG).show();
                                // Αν έπεσε ο server, του δίνουμε πίσω τα λεφτά
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

    private void updateBalanceUI() {
        tvBalance.setText("Current Balance: " + currentBalance + " FUN");
    }
}