package com.example.proyectopenka

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Credits : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnGithub: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_credits)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btnbackcredits)
        btnGithub = findViewById(R.id.btnGithub)

        btnBack.setOnClickListener { finish() }

        btnGithub.setOnClickListener {
            // Reemplaza con tu URL real
            val url = "https://github.com/lacteada"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
