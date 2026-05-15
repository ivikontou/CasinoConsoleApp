package com.example.casinoconsoleapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Φορτώνει το XML που μόλις έφτιαξες
        setContentView(R.layout.activity_main)

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