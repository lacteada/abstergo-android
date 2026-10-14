package cl.lacteada.abstergo

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.Attention
import cl.lacteada.abstergo.data.AttentionSubtype
import cl.lacteada.abstergo.data.AttentionType
import cl.lacteada.abstergo.data.Delegation
import cl.lacteada.abstergo.data.Neighbor
import cl.lacteada.abstergo.dominio.Catalogos
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch

class AtencionForm : ActividadBase() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var title: TextView
    private lateinit var vecinoSpinner: Spinner
    private lateinit var tipoSpinner: Spinner
    private lateinit var subSpinner: Spinner
    private lateinit var delegacionSpinner: Spinner
    private lateinit var estadoSpinner: Spinner
    private lateinit var canalSpinner: Spinner
    private lateinit var motivoInput: EditText
    private lateinit var detalleInput: EditText

    private var id: Int? = null
    private var vecinos: List<Neighbor> = emptyList()
    private var tipos: List<AttentionType> = emptyList()
    private var subs: List<AttentionSubtype> = emptyList()
    private var delegaciones: List<Delegation> = emptyList()
    private var funcionarioId: Int = 0
    private var ultimoTipo = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atencion_form)

        btnBack = findViewById(R.id.btnbackatencionform)
        btnSave = findViewById(R.id.btnsaveatencion)
        title = findViewById(R.id.titleatencionform)
        vecinoSpinner = findViewById(R.id.formAtencionVecino)
        tipoSpinner = findViewById(R.id.formAtencionTipo)
        subSpinner = findViewById(R.id.formAtencionSub)
        delegacionSpinner = findViewById(R.id.formAtencionDelegacion)
        estadoSpinner = findViewById(R.id.formAtencionEstado)
        canalSpinner = findViewById(R.id.formAtencionCanal)
        motivoInput = findViewById(R.id.formAtencionMotivo)
        detalleInput = findViewById(R.id.formAtencionDetalle)

        estadoSpinner.adapter = adaptador(Catalogos.ESTADOS)
        canalSpinner.adapter = adaptador(Catalogos.CANALES)

        id = intent.getIntExtra("id", 0).takeIf { it > 0 }

        if (id != null) {
            title.text = "Editar atención"
            motivoInput.setText(intent.getStringExtra("reason").orEmpty())
            detalleInput.setText(intent.getStringExtra("detail").orEmpty())
        }

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { guardar() }

        tipoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(padre: AdapterView<*>?, vista: View?, pos: Int, id: Long) {
                // Solo repinta si el usuario cambió el tipo (no en la precarga).
                if (pos != ultimoTipo) {
                    ultimoTipo = pos
                    pintarSubs(pos)
                }
            }

            override fun onNothingSelected(padre: AdapterView<*>?) {}
        }

        cargarCatalogos()
    }

    private fun adaptador(valores: List<String>) =
        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, valores)

    private fun cargarCatalogos() {
        lifecycleScope.launch {
            try {
                funcionarioId = if (id != null) {
                    intent.getIntExtra("staff", 0)
                } else {
                    Api.service.me().id
                }
                vecinos = Api.service.neighbors().results
                tipos = Api.service.attentionTypes().results
                subs = Api.service.attentionSubtypes().results
                delegaciones = Api.service.delegations().results

                vecinoSpinner.adapter = adaptador(vecinos.map { "${it.name} (${it.rut})" })
                delegacionSpinner.adapter = adaptador(delegaciones.map { it.name })

                val tipoPos = tipos
                    .indexOfFirst { it.id == intent.getIntExtra("attention_type", 0) }
                    .coerceAtLeast(0)
                tipoSpinner.adapter = adaptador(tipos.map { it.name })
                ultimoTipo = tipoPos
                tipoSpinner.setSelection(tipoPos)
                pintarSubs(tipoPos)

                if (id != null) {
                    val subPos = subs
                        .filter { it.attentionType == tipos.getOrNull(tipoPos)?.id }
                        .indexOfFirst { it.id == intent.getIntExtra("attention_subtype", 0) }
                        .let { if (it >= 0) it + 1 else 0 }

                    vecinoSpinner.setSelection(
                        vecinos.indexOfFirst { it.id == intent.getIntExtra("neighbor", 0) }
                            .coerceAtLeast(0)
                    )
                    delegacionSpinner.setSelection(
                        delegaciones.indexOfFirst { it.id == intent.getIntExtra("delegation", 0) }
                            .coerceAtLeast(0)
                    )
                    subSpinner.setSelection(subPos)
                    estadoSpinner.setSelection(
                        Catalogos.ESTADOS.indexOf(intent.getStringExtra("status")).coerceAtLeast(0)
                    )
                    canalSpinner.setSelection(
                        Catalogos.CANALES.indexOf(intent.getStringExtra("channel")).coerceAtLeast(0)
                    )
                }
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso("Sin conexión", "No se pudieron cargar los catálogos.")
            }
        }
    }

    private fun pintarSubs(posicionTipo: Int) {
        val tipoId = tipos.getOrNull(posicionTipo)?.id
        val delTipo = subs.filter { it.attentionType == tipoId }
        subSpinner.adapter = adaptador(listOf("—") + delTipo.map { it.name })
    }

    private fun subSeleccionada(): Int? {
        val posicion = subSpinner.selectedItemPosition
        if (posicion <= 0) return null
        val tipoId = tipos[tipoSpinner.selectedItemPosition].id
        return subs.filter { it.attentionType == tipoId }.getOrNull(posicion - 1)?.id
    }

    private fun guardar() {
        motivoInput.error = null
        val motivo = motivoInput.text.toString().trim()

        when {
            vecinos.isEmpty() || tipos.isEmpty() || delegaciones.isEmpty() -> {
                aviso("Faltan datos", "No se cargaron los catálogos.")
            }

            motivo.isEmpty() -> {
                motivoInput.error = "El motivo es obligatorio"
                motivoInput.requestFocus()
            }

            funcionarioId == 0 -> {
                aviso("Sesión incompleta", "No se pudo identificar al funcionario.")
            }

            else -> enviar(
                Attention(
                    id = id,
                    neighbor = vecinos[vecinoSpinner.selectedItemPosition].id!!,
                    attentionType = tipos[tipoSpinner.selectedItemPosition].id,
                    attentionSubtype = subSeleccionada(),
                    delegation = delegaciones[delegacionSpinner.selectedItemPosition].id,
                    staff = funcionarioId,
                    reason = motivo,
                    detail = detalleInput.text.toString().trim(),
                    status = Catalogos.ESTADOS[estadoSpinner.selectedItemPosition],
                    channel = Catalogos.CANALES[canalSpinner.selectedItemPosition],
                )
            )
        }
    }

    private fun enviar(atencion: Attention) {
        val dialogo = dialogoProgreso("Guardando...")
        lifecycleScope.launch {
            try {
                if (id != null) {
                    Api.service.updateCase(id!!, atencion)
                } else {
                    Api.service.createCase(atencion)
                }
                dialogo.terminar(
                    SweetAlertDialog.SUCCESS_TYPE,
                    "Guardado",
                    "La atención quedó registrada.",
                ) { finish() }
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                dialogo.terminar(
                    SweetAlertDialog.ERROR_TYPE,
                    tituloDeError(e),
                    textoDeError(e, "El servidor rechazó los datos."),
                )
            }
        }
    }
}
