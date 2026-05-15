package com.example.casinoconsoleapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.Gravity
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Φορτώνει το XML
        setContentView(R.layout.activity_main)

        // --- ΕΝΑΡΞΗ ΠΡΟΣΘΗΚΗΣ ΓΙΑ ΤΟ PADDING (Κάμερα + Αριστερά/Δεξιά) ---

        // Υπολογισμός 16dp σε pixels, ώστε να δείχνει σωστά σε όλες τις οθόνες
        val paddingPx = (16 * resources.displayMetrics.density).toInt()

        // Βρίσκουμε το κεντρικό Layout.
        // ΠΡΟΣΟΧΗ: Πρέπει στο activity_main.xml, το πρώτο-πρώτο Layout (π.χ. LinearLayout) να έχει android:id="@+id/main_root"
        val rootLayout = findViewById<View>(R.id.main_root)

        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                // Εφαρμογή padding: Κρατάμε τα top/bottom του συστήματος και προσθέτουμε +16dp αριστερά και δεξιά
                v.setPadding(
                    systemBars.left + paddingPx,
                    systemBars.top,
                    systemBars.right + paddingPx,
                    systemBars.bottom
                )
                insets
            }
        }

        // --- ΤΕΛΟΣ ΠΡΟΣΘΗΚΗΣ ΓΙΑ ΤΟ PADDING ---



        val etPlayerName = findViewById<EditText>(R.id.etPlayerName)
        val etMasterIp = findViewById<EditText>(R.id.etMasterIp) // ΝΕΟ
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val name = etPlayerName.text.toString().trim()
            val ip = etMasterIp.text.toString().trim() // ΝΕΟ

            if (name.isNotEmpty() && ip.isNotEmpty()) {
                // Ενημερώνουμε το δίκτυο με τη νέα IP
                com.example.casinoconsoleapp.network.TcpClientManager.MASTER_IP = ip

                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("PLAYER_NAME", name)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter Name and IP!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}