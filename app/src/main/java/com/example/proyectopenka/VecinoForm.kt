package com.example.proyectopenka

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.proyectopenka.data.Api
import com.example.proyectopenka.data.Delegacion
import com.example.proyectopenka.data.VecinoRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VecinoForm : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var title: TextView
    private lateinit var nombreInput: EditText
    private lateinit var rutInput: EditText
    private lateinit var direccionInput: EditText
    private lateinit var telefonoInput: EditText
    private lateinit var gestionInput: EditText
    private lateinit var territorioSpinner: Spinner
    private lateinit var activoSwitch: Switch

    private var id: Int? = null
    private var delegaciones: List<Delegacion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vecino_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        btnBack = findViewById(R.id.btnbackvecinoform)
        btnSave = findViewById(R.id.btnsavevecino)
        title = findViewById(R.id.titlevecinoform)
        nombreInput = findViewById(R.id.formVecinoNombre)
        rutInput = findViewById(R.id.formVecinoRut)
        direccionInput = findViewById(R.id.formVecinoDireccion)
        telefonoInput = findViewById(R.id.formVecinoTelefono)
        gestionInput = findViewById(R.id.formVecinoGestion)
        territorioSpinner = findViewById(R.id.formVecinoTerritorio)
        activoSwitch = findViewById(R.id.formVecinoActivo)

        id = intent.getIntExtra("id", 0).takeIf { it > 0 }

        if (id != null) {
            title.text = "Editar vecino"
            nombreInput.setText(intent.getStringExtra("nombre").orEmpty())
            rutInput.setText(intent.getStringExtra("rut").orEmpty())
            direccionInput.setText(intent.getStringExtra("direccion").orEmpty())
            telefonoInput.setText(intent.getStringExtra("telefono").orEmpty())
            gestionInput.setText(intent.getStringExtra("tipo_gestion").orEmpty())
            activoSwitch.isChecked = intent.getStringExtra("estado") != "Inactivo"
        }

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { guardar() }

        cargarDelegaciones()
    }

    private fun cargarDelegaciones() {
        lifecycleScope.launch {
            try {
                delegaciones = Api.service.delegaciones().results
                territorioSpinner.adapter = ArrayAdapter(
                    this@VecinoForm,
                    android.R.layout.simple_spinner_dropdown_item,
                    delegaciones.map { it.nombre },
                )
                val actual = intent.getIntExtra("territorio", 0)
                val indice = delegaciones.indexOfFirst { it.id == actual }
                if (indice >= 0) territorioSpinner.setSelection(indice)
            } catch (e: Exception) {
                aviso(SweetAlertDialog.ERROR_TYPE, "Sin conexión", "No se pudieron cargar las delegaciones.")
            }
        }
    }

    private fun guardar() {
        nombreInput.error = null
        rutInput.error = null
        gestionInput.error = null

        val nombre = nombreInput.text.toString().trim()
        val rut = rutInput.text.toString().trim()
        val direccion = direccionInput.text.toString().trim()
        val telefono = telefonoInput.text.toString().trim()
        val gestion = gestionInput.text.toString().trim()
        val estado = if (activoSwitch.isChecked) "Activo" else "Inactivo"

        when {
            nombre.length < 2 -> {
                nombreInput.error = "Mínimo 2 caracteres"
                nombreInput.requestFocus()
                return
            }
            !isValidRut(rut) -> {
                rutInput.error = "RUT inválido (ej: 12345678-5)"
                rutInput.requestFocus()
                return
            }
            delegaciones.isEmpty() -> {
                aviso(SweetAlertDialog.ERROR_TYPE, "Falta la delegación", "No hay delegaciones disponibles.")
                return
            }
        }

        val territorio = delegaciones[territorioSpinner.selectedItemPosition].id
        val cuerpo = VecinoRequest(nombre, rut, direccion, telefono, territorio, gestion, estado)

        val dialogo = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        dialogo.titleText = "Guardando..."
        dialogo.setCancelable(false)
        dialogo.show()

        lifecycleScope.launch {
            try {
                if (id != null) {
                    Api.service.actualizarVecino(id!!, cuerpo)
                } else {
                    Api.service.crearVecino(cuerpo)
                }
                dialogo.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                dialogo.titleText = "Guardado"
                dialogo.contentText = "El vecino quedó registrado."
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

    private fun aviso(tipo: Int, titulo: String, texto: String) {
        SweetAlertDialog(this, tipo)
            .setTitleText(titulo)
            .setContentText(texto)
            .setConfirmText("Aceptar")
            .setConfirmClickListener { it.dismissWithAnimation() }
            .show()
    }

    private fun isValidRut(rut: String): Boolean {
        val clean = rut.replace(".", "").uppercase()
        val parts = clean.split("-")
        if (parts.size != 2) return false

        val body = parts[0]
        val dv = parts[1]
        if (body.isEmpty() || dv.isEmpty()) return false
        if (!body.all { it.isDigit() }) return false
        if (dv.length != 1 || (!dv[0].isDigit() && dv != "K")) return false

        var sum = 0
        var factor = 2
        for (i in body.length - 1 downTo 0) {
            sum += body[i].digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }

        val remainder = sum % 11
        val calculated = when (val result = 11 - remainder) {
            11 -> "0"
            10 -> "K"
            else -> result.toString()
        }

        return dv == calculated
    }
}
