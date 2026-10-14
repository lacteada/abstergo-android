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
import cl.lacteada.abstergo.data.Attention
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch

class AtencionCrud : ActividadBase() {

    private lateinit var btnBack: Button
    private lateinit var btnNueva: Button
    private lateinit var searchInput: EditText
    private lateinit var adapter: ListaAdapter<Attention>
    private var atenciones: List<Attention> = emptyList()
    private var nombresVecinos: Map<Int, String> = emptyMap()
    private var nombresTipos: Map<Int, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atenciones)

        btnBack = findViewById(R.id.btnbackatenciones)
        btnNueva = findViewById(R.id.btnnuevaatencion)
        searchInput = findViewById(R.id.searchInputAtenciones)

        if (!Permisos.puedeEscribir("cases")) btnNueva.visibility = View.GONE

        adapter = ListaAdapter(
            describir = { atencion ->
                val vecino = nombresVecinos[atencion.neighbor] ?: "vecino ${atencion.neighbor}"
                val tipo = nombresTipos[atencion.attentionType] ?: "tipo ${atencion.attentionType}"
                "$vecino · $tipo · ${atencion.status} · ${atencion.reason}"
            },
            alTocar = { mostrarOpciones(it) },
        )
        findViewById<RecyclerView>(R.id.listaAtenciones).apply {
            layoutManager = LinearLayoutManager(this@AtencionCrud)
            adapter = this@AtencionCrud.adapter
        }

        btnBack.setOnClickListener { finish() }
        btnNueva.setOnClickListener { startActivity(Intent(this, AtencionForm::class.java)) }
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
                nombresVecinos = Api.service.neighbors().results.associate { it.id!! to it.name }
                nombresTipos = Api.service.attentionTypes().results.associate { it.id to it.name }
                atenciones = Api.service.cases().results
                filtrar(searchInput.text.toString())
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso(tituloDeError(e), textoDeError(e, "No se pudo cargar la lista de atenciones."))
            }
        }
    }

    private fun filtrar(consulta: String) {
        val texto = consulta.trim().lowercase()
        val visibles = if (texto.isEmpty()) {
            atenciones
        } else {
            atenciones.filter {
                nombresVecinos[it.neighbor].orEmpty().lowercase().contains(texto) ||
                    it.reason.lowercase().contains(texto)
            }
        }
        adapter.setDatos(visibles)
    }

    private fun mostrarOpciones(atencion: Attention) {
        val vecino = nombresVecinos[atencion.neighbor] ?: "vecino ${atencion.neighbor}"
        if (!Permisos.puedeEscribir("cases")) {
            aviso(vecino, "${atencion.reason} · Tu rol solo permite ver.", SweetAlertDialog.WARNING_TYPE)
            return
        }
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(vecino)
            .setContentText("${atencion.reason} · ¿Qué desea hacer?")
            .setConfirmText("Editar")
            .setCancelText("Eliminar")
            .setConfirmClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                startActivity(Intent(this, AtencionForm::class.java).apply {
                    putExtra("id", atencion.id ?: 0)
                    putExtra("neighbor", atencion.neighbor)
                    putExtra("attention_type", atencion.attentionType)
                    putExtra("attention_subtype", atencion.attentionSubtype ?: 0)
                    putExtra("delegation", atencion.delegation)
                    putExtra("staff", atencion.staff)
                    putExtra("reason", atencion.reason)
                    putExtra("detail", atencion.detail)
                    putExtra("status", atencion.status)
                    putExtra("channel", atencion.channel)
                })
            }
            .setCancelClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                eliminar(atencion)
            }
            .show()
    }

    private fun eliminar(atencion: Attention) {
        val id = atencion.id ?: return
        lifecycleScope.launch {
            try {
                Api.service.deleteCase(id)
                cargar()
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso(tituloDeError(e), textoDeError(e, "No se pudo eliminar. Revise su conexión."))
            }
        }
    }
}
