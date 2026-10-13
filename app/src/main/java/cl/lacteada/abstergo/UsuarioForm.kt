package cl.lacteada.abstergo

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
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.Delegacion
import cl.lacteada.abstergo.data.Rol
import cl.lacteada.abstergo.data.UsuarioRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UsuarioForm : AppCompatActivity() {

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
    private var roles: List<Rol> = emptyList()
    private var delegaciones: List<Delegacion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_usuario_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

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
            nombreInput.setText(intent.getStringExtra("nombre").orEmpty())
            apellidoInput.setText(intent.getStringExtra("apellido").orEmpty())
            emailInput.setText(intent.getStringExtra("email").orEmpty())
            activoSwitch.isChecked = intent.getBooleanExtra("activo", true)
        }

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { guardar() }

        cargarCatalogos()
    }

    private fun cargarCatalogos() {
        lifecycleScope.launch {
            try {
                roles = Api.service.roles().results
                delegaciones = Api.service.delegaciones().results

                rolSpinner.adapter = ArrayAdapter(
                    this@UsuarioForm,
                    android.R.layout.simple_spinner_dropdown_item,
                    roles.map { it.nombre },
                )
                delegacionSpinner.adapter = ArrayAdapter(
                    this@UsuarioForm,
                    android.R.layout.simple_spinner_dropdown_item,
                    delegaciones.map { it.nombre },
                )

                val rolActual = intent.getIntExtra("rol", 0)
                roles.indexOfFirst { it.id == rolActual }
                    .takeIf { it >= 0 }
                    ?.let(rolSpinner::setSelection)

                val delegacionActual = intent.getIntExtra("delegacion", 0)
                delegaciones.indexOfFirst { it.id == delegacionActual }
                    .takeIf { it >= 0 }
                    ?.let(delegacionSpinner::setSelection)
            } catch (e: Exception) {
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
        val password = passwordInput.text.toString()
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

        when {
            nombre.length < 2 -> {
                nombreInput.error = "Mínimo 2 caracteres"
                nombreInput.requestFocus()
                return
            }
            apellido.length < 2 -> {
                apellidoInput.error = "Mínimo 2 caracteres"
                apellidoInput.requestFocus()
                return
            }
            !emailRegex.matches(email) -> {
                emailInput.error = "Correo inválido"
                emailInput.requestFocus()
                return
            }
            roles.isEmpty() || delegaciones.isEmpty() -> {
                aviso("Faltan datos", "No se cargaron roles o delegaciones.")
                return
            }
        }

        val rol = roles[rolSpinner.selectedItemPosition].id
        val delegacion = delegaciones[delegacionSpinner.selectedItemPosition].id
        val cuerpo = UsuarioRequest(
            email = email,
            nombre = nombre,
            apellido = apellido,
            rol = rol,
            delegacion = delegacion,
            // Vacío = no cambiar la contraseña (el backend lo ignora).
            password = password.ifEmpty { null },
        )

        val dialogo = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        dialogo.titleText = "Guardando..."
        dialogo.setCancelable(false)
        dialogo.show()

        lifecycleScope.launch {
            try {
                if (id != null) {
                    Api.service.actualizarUsuario(id!!, cuerpo)
                } else {
                    Api.service.crearUsuario(cuerpo)
                }
                dialogo.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                dialogo.titleText = "Guardado"
                dialogo.contentText = "El usuario quedó registrado."
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
