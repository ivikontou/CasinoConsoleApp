package com.example.casinoconsoleapp.network

import android.os.Handler
import android.os.Looper
import common.Game
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.Executors

object TcpClientManager {

    // ΠΡΟΣΟΧΗ: Το 10.0.2.2 είναι η IP που βλέπει ο Android Emulator το localhost του υπολογιστή σου!
    private const val MASTER_IP = "10.0.2.2"
    private const val MASTER_PORT = 4321

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    // Interface για να ειδοποιούμε το UI όταν έρθουν τα δεδομένα
    interface NetworkCallback<T> {
        fun onSuccess(result: T)
        fun onError(error: String)
    }

    fun searchGames(command: String, callback: NetworkCallback<List<Game>>) {
        executor.execute {
            try {
                // Ανοίγουμε Socket. Κλείνει αυτόματα χάρη στο `use` (σαν το try-with-resources της Java)
                Socket(MASTER_IP, MASTER_PORT).use { socket ->
                    socket.soTimeout = 5000 // 5 δευτερόλεπτα timeout
                    val out = ObjectOutputStream(socket.getOutputStream())
                    out.flush()
                    val input = ObjectInputStream(socket.getInputStream())

                    // Στέλνουμε το String (π.χ. "PLAYER_CMD|SEARCH|low|$$")
                    out.writeObject(command)
                    out.flush()

                    // Περιμένουμε (μπλοκάρει το background thread, ΟΧΙ το UI)
                    val response = input.readObject()

                    if (response is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val games = response as List<Game>
                        // Στέλνουμε το αποτέλεσμα στο Main Thread για να ενημερωθεί η οθόνη
                        mainHandler.post { callback.onSuccess(games) }
                    } else {
                        mainHandler.post { callback.onError("Άγνωστη μορφή δεδομένων από τον Master.") }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mainHandler.post { callback.onError("Σφάλμα σύνδεσης: ${e.message}") }
            }
        }
    }

    fun placeBet(command: String, callback: NetworkCallback<String>) {
        executor.execute {
            try {
                Socket(MASTER_IP, MASTER_PORT).use { socket ->
                    val out = ObjectOutputStream(socket.getOutputStream())
                    out.flush()
                    val input = ObjectInputStream(socket.getInputStream())

                    out.writeObject(command)
                    out.flush()

                    val response = input.readObject() as? String ?: "Άγνωστο αποτέλεσμα"
                    mainHandler.post { callback.onSuccess(response) }
                }
            } catch (e: Exception) {
                mainHandler.post { callback.onError("Σφάλμα πονταρίσματος: ${e.message}") }
            }
        }
    }
}