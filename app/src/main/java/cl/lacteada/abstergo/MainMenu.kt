package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.RefreshRequest
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

        // Los módulos aparecen recién cuando llega el perfil del usuario.
        btnVecinos.visibility = View.GONE
        btnAtenciones.visibility = View.GONE
        btnUsuarios.visibility = View.GONE

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

    override fun onResume() {
        super.onResume()
        if (irALoginSiExpiro()) return
        cargarPerfil()
    }

    private fun cargarPerfil() {
        lifecycleScope.launch {
            try {
                val perfil = Api.service.yo()
                Api.perfil = perfil
                menuTitle.text = "Bienvenid@ · ${perfil.rolCodigo ?: "sin rol"}"
                btnVecinos.visibility = mostrar(Permisos.puedeLeer("vecinos"))
                btnAtenciones.visibility = mostrar(Permisos.puedeLeer("atenciones"))
                btnUsuarios.visibility = mostrar(Permisos.puedeLeer("usuarios"))
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
            runCatching { refresh?.let { Api.service.cerrarSesion(RefreshRequest(it)) } }
            Api.sesion.limpiar()
            Api.perfil = null
            val intent = Intent(this@MainMenu, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
