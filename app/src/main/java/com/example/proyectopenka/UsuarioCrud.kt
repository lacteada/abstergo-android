package com.example.proyectopenka

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.proyectopenka.data.Api
import com.example.proyectopenka.data.Usuario
import kotlinx.coroutines.launch

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
        cargar()
    }

    private fun cargar() {
        lifecycleScope.launch {
            try {
                usuarios = Api.service.usuarios().results
                filtrar(searchInput.text.toString())
            } catch (e: Exception) {
                aviso("Sin conexión", "No se pudo cargar la lista de usuarios.")
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
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("${usuario.nombre} ${usuario.apellido}")
            .setContentText("${usuario.email ?: "sin correo"} · ¿Qué desea hacer?")
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
                aviso("No se pudo eliminar", "Revise su conexión e intente de nuevo.")
            }
        }
    }

    private fun aviso(titulo: String, texto: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(titulo)
            .setContentText(texto)
            .setConfirmText("Aceptar")
            .setConfirmClickListener { it.dismissWithAnimation() }
            .show()
    }
}
