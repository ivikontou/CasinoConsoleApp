package com.example.casinoconsoleapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.view.Gravity;
import android.graphics.Typeface;
import android.widget.LinearLayout;

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

<<<<<<< HEAD
        // Pairnoume to onoma apo to login
=======
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

        // Δυναμική προσθήκη Τίτλου
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
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d
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
        adapter = new GameAdapter(new ArrayList<>(), this::showBettingDialog);
        recyclerView.setAdapter(adapter);

<<<<<<< HEAD
        // Koumpi Prosthikis Xrimatwn (KALEI TO POP-UP PLEON)
=======
        // Κουμπί Προσθήκης Χρημάτων (Ανοίγει το νέο Pop-up)
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d
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
<<<<<<< HEAD
                    // SWSTO: Enimerwsi UI sto Main Thread
=======
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d
                    runOnUiThread(() -> adapter.setGames(result));
                }

                @Override
                public void onError(String error) {
<<<<<<< HEAD
                    runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Sfalma: " + error, Toast.LENGTH_LONG).show());
=======
                    runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Σφάλμα: " + error, Toast.LENGTH_LONG).show());
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d
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
<<<<<<< HEAD
                                    .setTitle("Apotelesma")
=======
                                    .setTitle("Αποτέλεσμα")
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d
                                    .setMessage(result)
                                    .setPositiveButton("OK", null)
                                    .show());
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
<<<<<<< HEAD
                                Toast.makeText(DashboardActivity.this, "Sfalma: " + error, Toast.LENGTH_LONG).show();
                                // An epese o server, tou dinoume pisw ta lefta
=======
                                Toast.makeText(DashboardActivity.this, "Σφάλμα: " + error, Toast.LENGTH_LONG).show();
                                // Αν έπεσε ο server, του δίνουμε πίσω τα λεφτά
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d
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

<<<<<<< HEAD
    // NEA SYNARTISI: To Pop-up Prosthikis Xrimatwn
=======
    // --- ΝΕΑ ΣΥΝΑΡΤΗΣΗ ΓΙΑ ΤΟ POP-UP ΠΡΟΣΘΗΚΗΣ ΧΡΗΜΑΤΩΝ ---
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d
    private void showAddTokensDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Tokens");

<<<<<<< HEAD
        final EditText input = new EditText(this);
        input.setHint("Enter amount to add");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

=======
        // Δημιουργία πεδίου κειμένου (EditText) για εισαγωγή αριθμού
        final EditText input = new EditText(this);
        input.setHint("Enter amount to add");
        // Περιορισμός του πληκτρολογίου μόνο σε αριθμούς και δεκαδικά
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Τι γίνεται όταν πατάει "ADD"
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d
        builder.setPositiveButton("ADD", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                double amountToAdd = Double.parseDouble(amountStr);
<<<<<<< HEAD
=======

                // Αποτροπή αρνητικών ποσών ή μηδενικών
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d
                if (amountToAdd > 0) {
                    currentBalance += amountToAdd;
                    updateBalanceUI();
                    Toast.makeText(this, "Added " + amountToAdd + " FUN!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please enter a valid amount!", Toast.LENGTH_SHORT).show();
                }
            }
        });

<<<<<<< HEAD
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
=======
        // Τι γίνεται όταν πατάει "Cancel"
        builder.setNegativeButton("Cancel", null);

        // Εμφάνιση του Dialog
        builder.show();
    }
    // --- ΤΕΛΟΣ ΝΕΑΣ ΣΥΝΑΡΤΗΣΗΣ ---
>>>>>>> bd26fc172a394affed1c5a85972e214eafd18f0d

    private void updateBalanceUI() {
        tvBalance.setText("Current Balance: " + currentBalance + " FUN");
    }
}