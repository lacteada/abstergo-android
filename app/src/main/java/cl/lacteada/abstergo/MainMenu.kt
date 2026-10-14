package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.RefreshRequest
import kotlinx.coroutines.launch

class MainMenu : ActividadBase() {

    private lateinit var menuTitle: TextView
    private lateinit var btnGestion: Button
    private lateinit var btnSensores: Button
    private lateinit var btnCreditos: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        menuTitle = findViewById(R.id.menu_title)
        btnGestion = findViewById(R.id.btn_gestion)
        btnSensores = findViewById(R.id.btn_sensores)
        btnCreditos = findViewById(R.id.btn_creditos)
        btnLogout = findViewById(R.id.btn_logout)

        btnGestion.setOnClickListener {
            startActivity(Intent(this, UserMenu::class.java))
        }
        btnSensores.setOnClickListener {
            startActivity(Intent(this, Sensors::class.java))
        }
        btnCreditos.setOnClickListener {
            startActivity(Intent(this, Credits::class.java))
        }
        btnLogout.setOnClickListener { cerrarSesion() }
    }

    override fun onResume() {
        super.onResume()
        if (irALoginSiExpiro()) return
        cargarPerfil()
    }

    private fun cargarPerfil() {
        lifecycleScope.launch {
            try {
                val perfil = Api.service.me()
                Api.perfil = perfil
                menuTitle.text = "Bienvenid@ · ${perfil.roleCode ?: "sin rol"}"
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                menuTitle.text = "Menú Principal"
            }
        }
    }

    private fun cerrarSesion() {
        val refresh = Api.sesion.refresh
        lifecycleScope.launch {
            // Si falla, igual se limpia la sesión local.
            runCatching { refresh?.let { Api.service.logout(RefreshRequest(it)) } }
            Api.sesion.limpiar()
            Api.perfil = null
            val intent = Intent(this@MainMenu, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
