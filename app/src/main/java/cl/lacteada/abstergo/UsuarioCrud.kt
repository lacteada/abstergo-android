package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.User
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch

class UsuarioCrud : ActividadBase() {

    private lateinit var btnBack: Button
    private lateinit var btnNuevo: Button
    private lateinit var searchInput: EditText
    private lateinit var adapter: ListaAdapter<User>
    private var usuarios: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)

        btnBack = findViewById(R.id.btnbackusuarios)
        btnNuevo = findViewById(R.id.btnnuevousuario)
        searchInput = findViewById(R.id.searchInputUsuarios)

        if (!Permisos.puedeEscribir("users")) btnNuevo.visibility = View.GONE

        adapter = ListaAdapter(
            describir = { usuario ->
                val estado = if (usuario.active) "activo" else "inactivo"
                "${usuario.firstName} ${usuario.lastName} · " +
                    "${usuario.email ?: "sin correo"} · $estado"
            },
            alTocar = { mostrarOpciones(it) },
        )
        findViewById<RecyclerView>(R.id.listaUsuarios).apply {
            layoutManager = LinearLayoutManager(this@UsuarioCrud)
            adapter = this@UsuarioCrud.adapter
        }

        btnBack.setOnClickListener { finish() }
        btnNuevo.setOnClickListener { startActivity(Intent(this, UsuarioForm::class.java)) }
        searchInput.addTextChangedListener(filtro { filtrar(it) })
    }

    override fun onResume() {
        super.onResume()
        if (irALoginSiExpiro()) return
        cargar()
    }

    private fun cargar() {
        lifecycleScope.launch {
            try {
                usuarios = Api.service.users().results
                filtrar(searchInput.text.toString())
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso(tituloDeError(e), textoDeError(e, "No se pudo cargar la lista de usuarios."))
            }
        }
    }

    private fun filtrar(consulta: String) {
        val texto = consulta.trim().lowercase()
        val visibles = if (texto.isEmpty()) {
            usuarios
        } else {
            usuarios.filter {
                "${it.firstName} ${it.lastName}".lowercase().contains(texto) ||
                    (it.email ?: "").lowercase().contains(texto)
            }
        }
        adapter.setDatos(visibles)
    }

    private fun mostrarOpciones(usuario: User) {
        val rotulo = "${usuario.firstName} ${usuario.lastName}"
        val correo = usuario.email ?: "sin correo"
        if (!Permisos.puedeEscribir("users")) {
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
                    putExtra("id", usuario.id ?: 0)
                    putExtra("email", usuario.email)
                    putExtra("first_name", usuario.firstName)
                    putExtra("last_name", usuario.lastName)
                    putExtra("role", usuario.role ?: 0)
                    putExtra("delegation", usuario.delegation ?: 0)
                    putExtra("active", usuario.active)
                })
            }
            .setCancelClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                eliminar(usuario)
            }
            .show()
    }

    private fun eliminar(usuario: User) {
        val id = usuario.id ?: return
        lifecycleScope.launch {
            try {
                Api.service.deleteUser(id)
                cargar()
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso(tituloDeError(e), textoDeError(e, "No se pudo eliminar. Revise su conexión."))
            }
        }
    }
}
