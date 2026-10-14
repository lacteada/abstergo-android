package cl.lacteada.abstergo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.Delegation
import cl.lacteada.abstergo.data.Neighbor
import cl.lacteada.abstergo.dominio.Rut
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch

class VecinoForm : ActividadBase() {

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
    private var delegaciones: List<Delegation> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vecino_form)

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
            nombreInput.setText(intent.getStringExtra("name").orEmpty())
            rutInput.setText(intent.getStringExtra("rut").orEmpty())
            direccionInput.setText(intent.getStringExtra("address").orEmpty())
            telefonoInput.setText(intent.getStringExtra("phone").orEmpty())
            gestionInput.setText(intent.getStringExtra("management_type").orEmpty())
            activoSwitch.isChecked = intent.getStringExtra("status") != "Inactivo"
        }

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { guardar() }
        cargarDelegaciones()
    }

    private fun cargarDelegaciones() {
        lifecycleScope.launch {
            try {
                delegaciones = Api.service.delegations().results
                territorioSpinner.adapter = ArrayAdapter(
                    this@VecinoForm,
                    android.R.layout.simple_spinner_dropdown_item,
                    delegaciones.map { it.name },
                )
                val indice = delegaciones.indexOfFirst { it.id == intent.getIntExtra("territory", 0) }
                if (indice >= 0) territorioSpinner.setSelection(indice)
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso("Sin conexión", "No se pudieron cargar las delegaciones.")
            }
        }
    }

    private fun guardar() {
        nombreInput.error = null
        rutInput.error = null

        val nombre = nombreInput.text.toString().trim()
        val rut = rutInput.text.toString().trim()

        when {
            nombre.length < 2 -> {
                nombreInput.error = "Mínimo 2 caracteres"
                nombreInput.requestFocus()
            }

            !Rut.esValido(rut) -> {
                rutInput.error = "RUT inválido (ej: 12345678-5)"
                rutInput.requestFocus()
            }

            delegaciones.isEmpty() -> {
                aviso("Falta la delegación", "No hay delegaciones disponibles.")
            }

            else -> enviar(
                Neighbor(
                    id = id,
                    name = nombre,
                    rut = Rut.normalizar(rut),
                    address = direccionInput.text.toString().trim(),
                    phone = telefonoInput.text.toString().trim(),
                    territory = delegaciones[territorioSpinner.selectedItemPosition].id,
                    managementType = gestionInput.text.toString().trim(),
                    status = if (activoSwitch.isChecked) "Activo" else "Inactivo",
                )
            )
        }
    }

    private fun enviar(vecino: Neighbor) {
        val dialogo = dialogoProgreso("Guardando...")
        lifecycleScope.launch {
            try {
                if (id != null) {
                    Api.service.updateNeighbor(id!!, vecino)
                } else {
                    Api.service.createNeighbor(vecino)
                }
                dialogo.terminar(
                    SweetAlertDialog.SUCCESS_TYPE,
                    "Guardado",
                    "El vecino quedó registrado.",
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
