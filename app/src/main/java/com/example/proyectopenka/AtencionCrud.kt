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
import com.example.proyectopenka.data.Atencion
import kotlinx.coroutines.launch

class AtencionCrud : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnNueva: Button
    private lateinit var searchInput: EditText
    private lateinit var adapter: AtencionAdapter
    private var atenciones: List<Atencion> = emptyList()
    private var nombresVecinos: Map<Int, String> = emptyMap()
    private var nombresTipos: Map<Int, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_atenciones)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        btnBack = findViewById(R.id.btnbackatenciones)
        btnNueva = findViewById(R.id.btnnuevaatencion)
        searchInput = findViewById(R.id.searchInputAtenciones)

        adapter = AtencionAdapter(
            describir = { atencion ->
                val vecino = nombresVecinos[atencion.vecino] ?: "vecino ${atencion.vecino}"
                val tipo = nombresTipos[atencion.tipo_atencion] ?: "tipo ${atencion.tipo_atencion}"
                "$vecino · $tipo · ${atencion.estado} · ${atencion.motivo}"
            },
            alTocar = { atencion -> mostrarOpciones(atencion) },
        )
        findViewById<RecyclerView>(R.id.listaAtenciones).apply {
            layoutManager = LinearLayoutManager(this@AtencionCrud)
            adapter = this@AtencionCrud.adapter
        }

        btnBack.setOnClickListener { finish() }
        btnNueva.setOnClickListener {
            startActivity(Intent(this, AtencionForm::class.java))
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
                nombresVecinos = Api.service.vecinos().results.associate { it.id to it.nombre }
                nombresTipos = Api.service.tiposAtencion().results.associate { it.id to it.nombre }
                atenciones = Api.service.atenciones().results
                filtrar(searchInput.text.toString())
            } catch (e: Exception) {
                aviso("Sin conexión", "No se pudo cargar la lista de atenciones.")
            }
        }
    }

    private fun filtrar(consulta: String) {
        val texto = consulta.trim().lowercase()
        val filtradas = if (texto.isEmpty()) {
            atenciones
        } else {
            atenciones.filter {
                val vecino = nombresVecinos[it.vecino].orEmpty()
                vecino.lowercase().contains(texto) || it.motivo.lowercase().contains(texto)
            }
        }
        adapter.setDatos(filtradas)
    }

    private fun mostrarOpciones(atencion: Atencion) {
        val vecino = nombresVecinos[atencion.vecino] ?: "vecino ${atencion.vecino}"
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(vecino)
            .setContentText("${atencion.motivo} · ¿Qué desea hacer?")
            .setConfirmText("Eliminar")
            .setCancelText("Cerrar")
            .setConfirmClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                eliminar(atencion)
            }
            .setCancelClickListener { it.dismissWithAnimation() }
            .show()
    }

    private fun eliminar(atencion: Atencion) {
        lifecycleScope.launch {
            try {
                Api.service.eliminarAtencion(atencion.id)
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
