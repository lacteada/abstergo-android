package cl.lacteada.abstergo

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
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.Vecino
import kotlinx.coroutines.launch

class VecinoCrud : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnNuevo: Button
    private lateinit var searchInput: EditText
    private lateinit var adapter: VecinoAdapter
    private var vecinos: List<Vecino> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vecino_crud)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        btnBack = findViewById(R.id.btnbackvecinos)
        btnNuevo = findViewById(R.id.btnnuevovecino)
        searchInput = findViewById(R.id.searchInputVecinos)

        adapter = VecinoAdapter { vecino -> mostrarOpciones(vecino) }
        findViewById<RecyclerView>(R.id.listaVecinos).apply {
            layoutManager = LinearLayoutManager(this@VecinoCrud)
            adapter = this@VecinoCrud.adapter
        }

        btnBack.setOnClickListener { finish() }
        btnNuevo.setOnClickListener {
            startActivity(Intent(this, VecinoForm::class.java))
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
                vecinos = Api.service.vecinos().results
                filtrar(searchInput.text.toString())
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso("Sin conexión", "No se pudo cargar la lista de vecinos.")
            }
        }
    }

    private fun filtrar(consulta: String) {
        val texto = consulta.trim().lowercase()
        val filtrados = if (texto.isEmpty()) {
            vecinos
        } else {
            vecinos.filter {
                it.nombre.lowercase().contains(texto) || it.rut.lowercase().contains(texto)
            }
        }
        adapter.setDatos(filtrados)
    }

    private fun mostrarOpciones(vecino: Vecino) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(vecino.nombre)
            .setContentText("${vecino.rut} · ¿Qué desea hacer?")
            .setConfirmText("Editar")
            .setCancelText("Eliminar")
            .setConfirmClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                startActivity(Intent(this, VecinoForm::class.java).apply {
                    putExtra("id", vecino.id)
                    putExtra("nombre", vecino.nombre)
                    putExtra("rut", vecino.rut)
                    putExtra("direccion", vecino.direccion)
                    putExtra("telefono", vecino.telefono)
                    putExtra("territorio", vecino.territorio)
                    putExtra("tipo_gestion", vecino.tipo_gestion)
                    putExtra("estado", vecino.estado)
                })
            }
            .setCancelClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                eliminar(vecino)
            }
            .show()
    }

    private fun eliminar(vecino: Vecino) {
        lifecycleScope.launch {
            try {
                Api.service.eliminarVecino(vecino.id)
                cargar()
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
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
