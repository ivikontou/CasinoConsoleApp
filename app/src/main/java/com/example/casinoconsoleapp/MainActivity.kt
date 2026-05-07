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
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val name = etPlayerName.text.toString().trim()
            if (name.isNotEmpty()) {
                // Σε πηγαίνει στο DashboardActivity.java
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("PLAYER_NAME", name)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}