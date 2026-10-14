package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.RefreshRequest
import kotlinx.coroutines.launch

class MainMenu : ActividadBase() {

    private lateinit var menuTitle: TextView
    private lateinit var btnNeighbors: Button
    private lateinit var btnCases: Button
    private lateinit var btnUsers: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        menuTitle = findViewById(R.id.menu_title)
        btnNeighbors = findViewById(R.id.btn_vecinos)
        btnCases = findViewById(R.id.btn_atenciones)
        btnUsers = findViewById(R.id.btn_usuarios)
        btnLogout = findViewById(R.id.btn_logout)

        menuTitle.text = "Bienvenid@"

        // Los módulos aparecen recién cuando llega el perfil del usuario.
        btnNeighbors.visibility = View.GONE
        btnCases.visibility = View.GONE
        btnUsers.visibility = View.GONE

        btnNeighbors.setOnClickListener {
            startActivity(Intent(this, VecinoCrud::class.java))
        }
        btnCases.setOnClickListener {
            startActivity(Intent(this, AtencionCrud::class.java))
        }
        btnUsers.setOnClickListener {
            startActivity(Intent(this, UsuarioCrud::class.java))
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
                btnNeighbors.visibility = mostrar(Permisos.puedeLeer("neighbors"))
                btnCases.visibility = mostrar(Permisos.puedeLeer("cases"))
                btnUsers.visibility = mostrar(Permisos.puedeLeer("users"))
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                // Sin perfil no se ofrece ningún módulo: queda cerrar sesión.
                menuTitle.text = "No se pudo cargar el perfil"
            }
        }
    }

    private fun mostrar(si: Boolean) = if (si) View.VISIBLE else View.GONE

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
