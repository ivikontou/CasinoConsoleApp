package com.example.casinoconsoleapp;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class MockNetwork {
    // Προσομοίωση αναζήτησης με φίλτρα
    public static List<Game> sendSearch(String risk, String limit) {
        List<Game> dummyGames = new ArrayList<>();
        dummyGames.add(new Game("Mega Slots", "ProviderA", risk));
        dummyGames.add(new Game("Lucky 7s", "ProviderB", risk));
        dummyGames.add(new Game("Poker King", "ProviderA", risk));
        return dummyGames;
    }

    // Προσομοίωση πονταρίσματος
    public static void sendBet(Game game, double amount) {
        Log.d("MockNetwork", "Στάλθηκε ποντάρισμα " + amount + " στο παιχνίδι " + game.name);
    }
}
