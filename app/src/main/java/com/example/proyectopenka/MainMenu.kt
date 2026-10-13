package com.example.proyectopenka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyectopenka.data.Api
import com.example.proyectopenka.data.RefreshRequest
import kotlinx.coroutines.launch

class MainMenu : AppCompatActivity() {

    private lateinit var menuTitle: TextView
    private lateinit var btnVecinos: Button
    private lateinit var btnAtenciones: Button
    private lateinit var btnUsuarios: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        menuTitle = findViewById(R.id.menu_title)
        btnVecinos = findViewById(R.id.btn_vecinos)
        btnAtenciones = findViewById(R.id.btn_atenciones)
        btnUsuarios = findViewById(R.id.btn_usuarios)
        btnLogout = findViewById(R.id.btn_logout)

        menuTitle.text = "Bienvenid@"

        btnVecinos.setOnClickListener {
            startActivity(Intent(this, VecinoCrud::class.java))
        }

        btnAtenciones.setOnClickListener {
            startActivity(Intent(this, AtencionCrud::class.java))
        }

        btnUsuarios.setOnClickListener {
            startActivity(Intent(this, UsuarioCrud::class.java))
        }

        btnLogout.setOnClickListener { cerrarSesion() }
    }

    private fun cerrarSesion() {
        val refresh = Api.sesion.refresh
        lifecycleScope.launch {
            // Si falla, igual se limpia la sesión local.
            runCatching { refresh?.let { Api.service.cerrarSesion(RefreshRequest(it)) } }
            Api.sesion.limpiar()
            val intent = Intent(this@MainMenu, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
