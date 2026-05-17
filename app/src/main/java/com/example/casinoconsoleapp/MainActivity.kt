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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       //load to xml
        setContentView(R.layout.activity_main)
        //gia na emfanizetai kala se diaforetikes othones
        val paddingPx = (16 * resources.displayMetrics.density).toInt()

        val rootLayout = findViewById<View>(R.id.main_root)

        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                //efarmogi tou padding
                v.setPadding(
                    systemBars.left + paddingPx,
                    systemBars.top,
                    systemBars.right + paddingPx,
                    systemBars.bottom
                )
                insets
            }
        }

        val etPlayerName = findViewById<EditText>(R.id.etPlayerName)
        val etMasterIp = findViewById<EditText>(R.id.etMasterIp)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val name = etPlayerName.text.toString().trim()
            val ip = etMasterIp.text.toString().trim()

            if (name.isNotEmpty() && ip.isNotEmpty()) {
                //enimerwnoume to diktyo me thn nea ip
                com.example.casinoconsoleapp.network.TcpClientManager.MASTER_IP = ip

                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("PLAYER_NAME", name)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter Name and Master IP!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}