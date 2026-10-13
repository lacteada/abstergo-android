package com.example.proyectopenka

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.proyectopenka.data.Api
import com.example.proyectopenka.data.AtencionRequest
import com.example.proyectopenka.data.Delegacion
import com.example.proyectopenka.data.SubAtencion
import com.example.proyectopenka.data.TipoAtencion
import com.example.proyectopenka.data.Vecino
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AtencionForm : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var vecinoSpinner: Spinner
    private lateinit var tipoSpinner: Spinner
    private lateinit var subSpinner: Spinner
    private lateinit var delegacionSpinner: Spinner
    private lateinit var estadoSpinner: Spinner
    private lateinit var canalSpinner: Spinner
    private lateinit var motivoInput: EditText
    private lateinit var detalleInput: EditText

    private var vecinos: List<Vecino> = emptyList()
    private var tipos: List<TipoAtencion> = emptyList()
    private var subs: List<SubAtencion> = emptyList()
    private var delegaciones: List<Delegacion> = emptyList()
    private var funcionarioId: Int = 0

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

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { guardar() }

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
                funcionarioId = Api.service.yo().id
                vecinos = Api.service.vecinos().results
                tipos = Api.service.tiposAtencion().results
                subs = Api.service.subAtenciones().results
                delegaciones = Api.service.delegaciones().results

                vecinoSpinner.adapter = adaptador(vecinos.map { "${it.nombre} (${it.rut})" })
                tipoSpinner.adapter = adaptador(tipos.map { it.nombre })
                delegacionSpinner.adapter = adaptador(delegaciones.map { it.nombre })

                tipoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                        pintarSubs(pos)
                    }

                    override fun onNothingSelected(p: AdapterView<*>?) {}
                }
            } catch (e: Exception) {
                aviso("Sin conexión", "No se pudieron cargar los catálogos.")
            }
        }
    }

    private fun pintarSubs(posicionTipo: Int) {
        val tipoId = tipos.getOrNull(posicionTipo)?.id
        val delTipo = subs.filter { it.tipo_atencion == tipoId }
        subSpinner.adapter = adaptador(listOf("—") + delTipo.map { it.nombre })
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

        val seleccionSub = subSpinner.selectedItemPosition
        val subId = if (seleccionSub <= 0) null else {
            val tipoId = tipos[tipoSpinner.selectedItemPosition].id
            subs.filter { it.tipo_atencion == tipoId }.getOrNull(seleccionSub - 1)?.id
        }

        val cuerpo = AtencionRequest(
            vecino = vecinos[vecinoSpinner.selectedItemPosition].id,
            tipo_atencion = tipos[tipoSpinner.selectedItemPosition].id,
            sub_atencion = subId,
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
                Api.service.crearAtencion(cuerpo)
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
