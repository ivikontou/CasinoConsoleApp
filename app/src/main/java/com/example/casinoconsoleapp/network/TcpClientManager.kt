package com.example.casinoconsoleapp.network

import android.os.Handler
import android.os.Looper
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.Executors
import common.Game

object TcpClientManager {

    var MASTER_IP = "10.0.2.2" //proepilogi gia test runs
    private const val MASTER_PORT = 4321

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    //interface gia na eidopoioume to ui otan erthoun ta dedomena
    interface NetworkCallback<T> {
        fun onSuccess(result: T)
        fun onError(error: String)
    }

    fun searchGames(command: String, callback: NetworkCallback<List<Game>>) {
        executor.execute {
            try {
                //anoigoume socket kai tha klisei automata me to use
                Socket(MASTER_IP, MASTER_PORT).use { socket ->
                    socket.soTimeout = 5000 // 5 δευτερόλεπτα timeout
                    val out = ObjectOutputStream(socket.getOutputStream())
                    out.flush()
                    val input = ObjectInputStream(socket.getInputStream())

                    //stelnoume to string
                    out.writeObject(command)
                    out.flush()

                    //perimenoume (blockarei to background thread mono)
                    val response = input.readObject()

                    if (response is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val games = response as List<Game>
                        //send apotelesma sto main thread gia na enimerwthei h othoni
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