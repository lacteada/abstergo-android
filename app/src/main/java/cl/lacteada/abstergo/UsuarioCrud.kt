package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.Usuario
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UsuarioCrud : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnNuevo: Button
    private lateinit var searchInput: EditText
    private lateinit var adapter: UsuarioAdapter
    private var usuarios: List<Usuario> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_usuarios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        btnBack = findViewById(R.id.btnbackusuarios)
        btnNuevo = findViewById(R.id.btnnuevousuario)
        searchInput = findViewById(R.id.searchInputUsuarios)

        if (!Permisos.puedeEscribir("usuarios")) btnNuevo.visibility = View.GONE

        adapter = UsuarioAdapter { usuario -> mostrarOpciones(usuario) }
        findViewById<RecyclerView>(R.id.listaUsuarios).apply {
            layoutManager = LinearLayoutManager(this@UsuarioCrud)
            adapter = this@UsuarioCrud.adapter
        }

        btnBack.setOnClickListener { finish() }
        btnNuevo.setOnClickListener {
            startActivity(Intent(this, UsuarioForm::class.java))
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrar(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        if (irALoginSiExpiro()) return
        cargar()
    }

    private fun cargar() {
        lifecycleScope.launch {
            try {
                usuarios = Api.service.usuarios().results
                filtrar(searchInput.text.toString())
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso(tituloError(e), textoError(e, "No se pudo cargar la lista de usuarios."))
            }
        }
    }

    private fun filtrar(consulta: String) {
        val texto = consulta.trim().lowercase()
        val filtrados = if (texto.isEmpty()) {
            usuarios
        } else {
            usuarios.filter {
                "${it.nombre} ${it.apellido}".lowercase().contains(texto) ||
                    (it.email ?: "").lowercase().contains(texto)
            }
        }
        adapter.setDatos(filtrados)
    }

    private fun mostrarOpciones(usuario: Usuario) {
        val rotulo = "${usuario.nombre} ${usuario.apellido}"
        val correo = usuario.email ?: "sin correo"
        if (!Permisos.puedeEscribir("usuarios")) {
            aviso(rotulo, "$correo · Tu rol solo permite ver.", SweetAlertDialog.WARNING_TYPE)
            return
        }
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(rotulo)
            .setContentText("$correo · ¿Qué desea hacer?")
            .setConfirmText("Editar")
            .setCancelText("Eliminar")
            .setConfirmClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                startActivity(Intent(this, UsuarioForm::class.java).apply {
                    putExtra("id", usuario.id)
                    putExtra("email", usuario.email)
                    putExtra("nombre", usuario.nombre)
                    putExtra("apellido", usuario.apellido)
                    putExtra("rol", usuario.rol ?: 0)
                    putExtra("delegacion", usuario.delegacion ?: 0)
                    putExtra("activo", usuario.activo)
                })
            }
            .setCancelClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                eliminar(usuario)
            }
            .show()
    }

    private fun eliminar(usuario: Usuario) {
        lifecycleScope.launch {
            try {
                Api.service.eliminarUsuario(usuario.id)
                cargar()
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso(tituloError(e), textoError(e, "No se pudo eliminar. Revise su conexión."))
            }
        }
    }

    private fun tituloError(e: Exception) =
        if (e is HttpException && e.code() == 403) "Sin permiso" else "No se pudo completar"

    private fun textoError(e: Exception, alternativo: String) =
        if (e is HttpException && e.code() == 403) "Tu rol no permite esta acción." else alternativo

    private fun aviso(
        titulo: String,
        texto: String,
        tipo: Int = SweetAlertDialog.ERROR_TYPE,
    ) {
        SweetAlertDialog(this, tipo)
            .setTitleText(titulo)
            .setContentText(texto)
            .setConfirmText("Aceptar")
            .setConfirmClickListener { it.dismissWithAnimation() }
            .show()
    }
}
