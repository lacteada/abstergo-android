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
import cl.lacteada.abstergo.data.Neighbor
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch

class VecinoCrud : ActividadBase() {

    private lateinit var btnBack: Button
    private lateinit var btnNuevo: Button
    private lateinit var searchInput: EditText
    private lateinit var adapter: ListaAdapter<Neighbor>
    private var vecinos: List<Neighbor> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vecino_crud)

        btnBack = findViewById(R.id.btnbackvecinos)
        btnNuevo = findViewById(R.id.btnnuevovecino)
        searchInput = findViewById(R.id.searchInputVecinos)

        if (!Permisos.puedeEscribir("neighbors")) btnNuevo.visibility = View.GONE

        adapter = ListaAdapter(
            describir = { "${it.name} · ${it.rut} · ${it.status}" },
            alTocar = { mostrarOpciones(it) },
        )
        findViewById<RecyclerView>(R.id.listaVecinos).apply {
            layoutManager = LinearLayoutManager(this@VecinoCrud)
            adapter = this@VecinoCrud.adapter
        }

        btnBack.setOnClickListener { finish() }
        btnNuevo.setOnClickListener { startActivity(Intent(this, VecinoForm::class.java)) }
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
                vecinos = Api.service.neighbors().results
                filtrar(searchInput.text.toString())
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso(tituloDeError(e), textoDeError(e, "No se pudo cargar la lista de vecinos."))
            }
        }
    }

    private fun filtrar(consulta: String) {
        val texto = consulta.trim().lowercase()
        val visibles = if (texto.isEmpty()) {
            vecinos
        } else {
            vecinos.filter {
                it.name.lowercase().contains(texto) || it.rut.lowercase().contains(texto)
            }
        }
        adapter.setDatos(visibles)
    }

    private fun mostrarOpciones(vecino: Neighbor) {
        if (!Permisos.puedeEscribir("neighbors")) {
            aviso(
                vecino.name,
                "${vecino.rut} · Tu rol solo permite ver.",
                SweetAlertDialog.WARNING_TYPE,
            )
            return
        }
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(vecino.name)
            .setContentText("${vecino.rut} · ¿Qué desea hacer?")
            .setConfirmText("Editar")
            .setCancelText("Eliminar")
            .setConfirmClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                startActivity(Intent(this, VecinoForm::class.java).apply {
                    putExtra("id", vecino.id ?: 0)
                    putExtra("name", vecino.name)
                    putExtra("rut", vecino.rut)
                    putExtra("address", vecino.address)
                    putExtra("phone", vecino.phone)
                    putExtra("territory", vecino.territory)
                    putExtra("management_type", vecino.managementType)
                    putExtra("status", vecino.status)
                })
            }
            .setCancelClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                eliminar(vecino)
            }
            .show()
    }

    private fun eliminar(vecino: Neighbor) {
        val id = vecino.id ?: return
        lifecycleScope.launch {
            try {
                Api.service.deleteNeighbor(id)
                cargar()
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso(tituloDeError(e), textoDeError(e, "No se pudo eliminar. Revise su conexión."))
            }
        }
    }
}
