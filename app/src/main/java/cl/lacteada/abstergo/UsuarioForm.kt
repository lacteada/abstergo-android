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
import cl.lacteada.abstergo.data.Role
import cl.lacteada.abstergo.data.User
import cl.lacteada.abstergo.dominio.Patrones
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch

class UsuarioForm : ActividadBase() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var title: TextView
    private lateinit var nombreInput: EditText
    private lateinit var apellidoInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var rolSpinner: Spinner
    private lateinit var delegacionSpinner: Spinner
    private lateinit var activoSwitch: Switch

    private var id: Int? = null
    private var roles: List<Role> = emptyList()
    private var delegaciones: List<Delegation> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuario_form)

        btnBack = findViewById(R.id.btnbackusuarioform)
        btnSave = findViewById(R.id.btnsaveusuario)
        title = findViewById(R.id.titleusuarioform)
        nombreInput = findViewById(R.id.formUsuarioNombre)
        apellidoInput = findViewById(R.id.formUsuarioApellido)
        emailInput = findViewById(R.id.formUsuarioEmail)
        passwordInput = findViewById(R.id.formUsuarioPassword)
        rolSpinner = findViewById(R.id.formUsuarioRol)
        delegacionSpinner = findViewById(R.id.formUsuarioDelegacion)
        activoSwitch = findViewById(R.id.formUsuarioActivo)

        id = intent.getIntExtra("id", 0).takeIf { it > 0 }

        if (id != null) {
            title.text = "Editar usuario"
            nombreInput.setText(intent.getStringExtra("first_name").orEmpty())
            apellidoInput.setText(intent.getStringExtra("last_name").orEmpty())
            emailInput.setText(intent.getStringExtra("email").orEmpty())
            activoSwitch.isChecked = intent.getBooleanExtra("active", true)
        }

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { guardar() }
        cargarCatalogos()
    }

    private fun cargarCatalogos() {
        lifecycleScope.launch {
            try {
                roles = Api.service.roles().results
                delegaciones = Api.service.delegations().results

                rolSpinner.adapter = ArrayAdapter(
                    this@UsuarioForm,
                    android.R.layout.simple_spinner_dropdown_item,
                    roles.map { it.name },
                )
                delegacionSpinner.adapter = ArrayAdapter(
                    this@UsuarioForm,
                    android.R.layout.simple_spinner_dropdown_item,
                    delegaciones.map { it.name },
                )

                roles.indexOfFirst { it.id == intent.getIntExtra("role", 0) }
                    .takeIf { it >= 0 }
                    ?.let(rolSpinner::setSelection)
                delegaciones.indexOfFirst { it.id == intent.getIntExtra("delegation", 0) }
                    .takeIf { it >= 0 }
                    ?.let(delegacionSpinner::setSelection)
            } catch (e: Exception) {
                if (irALoginSiExpiro()) return@launch
                aviso("Sin conexión", "No se pudieron cargar roles y delegaciones.")
            }
        }
    }

    private fun guardar() {
        nombreInput.error = null
        apellidoInput.error = null
        emailInput.error = null

        val nombre = nombreInput.text.toString().trim()
        val apellido = apellidoInput.text.toString().trim()
        val email = emailInput.text.toString().trim()

        when {
            !Patrones.nombreValido(nombre) -> {
                nombreInput.error = "Solo letras y espacios"
                nombreInput.requestFocus()
            }

            !Patrones.nombreValido(apellido) -> {
                apellidoInput.error = "Solo letras y espacios"
                apellidoInput.requestFocus()
            }

            !Patrones.correoValido(email) -> {
                emailInput.error = "Correo inválido"
                emailInput.requestFocus()
            }

            roles.isEmpty() || delegaciones.isEmpty() -> {
                aviso("Faltan datos", "No se cargaron roles o delegaciones.")
            }

            else -> enviar(
                User(
                    id = id,
                    email = email,
                    firstName = nombre,
                    lastName = apellido,
                    role = roles[rolSpinner.selectedItemPosition].id,
                    delegation = delegaciones[delegacionSpinner.selectedItemPosition].id,
                    active = activoSwitch.isChecked,
                    // Vacío = no cambiar la contraseña (Gson no manda los nulos).
                    password = passwordInput.text.toString().ifEmpty { null },
                )
            )
        }
    }

    private fun enviar(usuario: User) {
        val dialogo = dialogoProgreso("Guardando...")
        lifecycleScope.launch {
            try {
                if (id != null) {
                    Api.service.updateUser(id!!, usuario)
                } else {
                    Api.service.createUser(usuario)
                }
                dialogo.terminar(
                    SweetAlertDialog.SUCCESS_TYPE,
                    "Guardado",
                    "El usuario quedó registrado.",
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
