package cl.lacteada.abstergo

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.AtencionRequest
import cl.lacteada.abstergo.data.Delegacion
import cl.lacteada.abstergo.data.SubAtencion
import cl.lacteada.abstergo.data.TipoAtencion
import cl.lacteada.abstergo.data.Vecino
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AtencionForm : AppCompatActivity() {

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
    private var vecinos: List<Vecino> = emptyList()
    private var tipos: List<TipoAtencion> = emptyList()
    private var subs: List<SubAtencion> = emptyList()
    private var delegaciones: List<Delegacion> = emptyList()
    private var funcionarioId: Int = 0
    private var ultimoTipo = -1

    private val estados = listOf("ingresada", "en_proceso", "cerrada", "derivada")
    private val canales = listOf("presencial", "telefono", "web")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_atencion_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

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

        estadoSpinner.adapter = adaptador(estados)
        canalSpinner.adapter = adaptador(canales)

        id = intent.getIntExtra("id", 0).takeIf { it > 0 }

        if (id != null) {
            title.text = "Editar atención"
            motivoInput.setText(intent.getStringExtra("motivo").orEmpty())
            detalleInput.setText(intent.getStringExtra("detalle").orEmpty())
        }

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { guardar() }

        tipoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, idLargo: Long) {
                // Solo repinta si el usuario cambió el tipo (no en la precarga).
                if (pos != ultimoTipo) {
                    ultimoTipo = pos
                    pintarSubs(pos)
                }
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        cargarCatalogos()
    }

    private fun adaptador(valores: List<String>) = ArrayAdapter(
        this,
        android.R.layout.simple_spinner_dropdown_item,
        valores,
    )

    private fun cargarCatalogos() {
        lifecycleScope.launch {
            try {
                funcionarioId = if (id != null) {
                    intent.getIntExtra("funcionario", 0)
                } else {
                    Api.service.yo().id
                }
                vecinos = Api.service.vecinos().results
                tipos = Api.service.tiposAtencion().results
                subs = Api.service.subAtenciones().results
                delegaciones = Api.service.delegaciones().results

                vecinoSpinner.adapter = adaptador(vecinos.map { "${it.nombre} (${it.rut})" })
                delegacionSpinner.adapter = adaptador(delegaciones.map { it.nombre })

                val tipoPos = tipos.indexOfFirst { it.id == intent.getIntExtra("tipo_atencion", 0) }
                    .coerceAtLeast(0)
                tipoSpinner.adapter = adaptador(tipos.map { it.nombre })
                ultimoTipo = tipoPos
                tipoSpinner.setSelection(tipoPos)
                pintarSubs(tipoPos)

                if (id != null) {
                    val tipoId = tipos.getOrNull(tipoPos)?.id
                    val subPos = subs.filter { it.tipo_atencion == tipoId }
                        .indexOfFirst { it.id == intent.getIntExtra("sub_atencion", 0) }
                        .let { if (it >= 0) it + 1 else 0 }

                    vecinoSpinner.setSelection(
                        vecinos.indexOfFirst { it.id == intent.getIntExtra("vecino", 0) }.coerceAtLeast(0)
                    )
                    delegacionSpinner.setSelection(
                        delegaciones.indexOfFirst { it.id == intent.getIntExtra("delegacion", 0) }
                            .coerceAtLeast(0)
                    )
                    subSpinner.setSelection(subPos)
                    estadoSpinner.setSelection(
                        estados.indexOf(intent.getStringExtra("estado")).coerceAtLeast(0)
                    )
                    canalSpinner.setSelection(
                        canales.indexOf(intent.getStringExtra("canal")).coerceAtLeast(0)
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
        val delTipo = subs.filter { it.tipo_atencion == tipoId }
        subSpinner.adapter = adaptador(listOf("—") + delTipo.map { it.nombre })
    }

    private fun subSeleccionada(): Int? {
        val posicion = subSpinner.selectedItemPosition
        if (posicion <= 0) return null
        val tipoId = tipos[tipoSpinner.selectedItemPosition].id
        return subs.filter { it.tipo_atencion == tipoId }.getOrNull(posicion - 1)?.id
    }

    private fun guardar() {
        motivoInput.error = null

        val motivo = motivoInput.text.toString().trim()
        val detalle = detalleInput.text.toString().trim()

        when {
            vecinos.isEmpty() -> {
                aviso("Faltan datos", "No hay vecinos para asociar.")
                return
            }
            tipos.isEmpty() -> {
                aviso("Faltan datos", "No hay tipos de atención.")
                return
            }
            delegaciones.isEmpty() -> {
                aviso("Faltan datos", "No hay delegaciones.")
                return
            }
            motivo.isEmpty() -> {
                motivoInput.error = "El motivo es obligatorio"
                motivoInput.requestFocus()
                return
            }
            funcionarioId == 0 -> {
                aviso("Sesión incompleta", "No se pudo identificar al funcionario.")
                return
            }
        }

        val cuerpo = AtencionRequest(
            vecino = vecinos[vecinoSpinner.selectedItemPosition].id,
            tipo_atencion = tipos[tipoSpinner.selectedItemPosition].id,
            sub_atencion = subSeleccionada(),
            delegacion = delegaciones[delegacionSpinner.selectedItemPosition].id,
            funcionario = funcionarioId,
            motivo = motivo,
            detalle = detalle,
            estado = estados[estadoSpinner.selectedItemPosition],
            canal = canales[canalSpinner.selectedItemPosition],
        )

        val dialogo = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        dialogo.titleText = "Guardando..."
        dialogo.setCancelable(false)
        dialogo.show()

        lifecycleScope.launch {
            try {
                if (id != null) {
                    Api.service.actualizarAtencion(id!!, cuerpo)
                } else {
                    Api.service.crearAtencion(cuerpo)
                }
                dialogo.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                dialogo.titleText = "Guardado"
                dialogo.contentText = "La atención quedó registrada."
                dialogo.confirmText = "Aceptar"
                dialogo.setConfirmClickListener {
                    it.dismissWithAnimation()
                    finish()
                }
            } catch (e: HttpException) {
                dialogo.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                dialogo.titleText = "No se pudo guardar"
                dialogo.contentText = "El servidor rechazó los datos (${e.code()})."
                dialogo.confirmText = "Aceptar"
                dialogo.setConfirmClickListener { it.dismissWithAnimation() }
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                dialogo.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                dialogo.titleText = "Sin conexión"
                dialogo.contentText = "No se pudo contactar al servidor."
                dialogo.confirmText = "Aceptar"
                dialogo.setConfirmClickListener { it.dismissWithAnimation() }
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
