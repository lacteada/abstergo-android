package cl.lacteada.abstergo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import cl.lacteada.abstergo.data.BdLocal
import cl.lacteada.abstergo.data.UsuarioLocal
import cl.lacteada.abstergo.dominio.Patrones
import cn.pedant.SweetAlert.SweetAlertDialog
import java.util.UUID

/**
 * Alta y edición de un usuario, sobre la base local.
 *
 * Lo guardado queda pendiente y el listado lo sube a MySQL cuando hay conexión.
 */
class UsuarioForm : ActividadBase() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var title: TextView
    private lateinit var nombreInput: EditText
    private lateinit var apellidoInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var activoSwitch: Switch

    private lateinit var bd: BdLocal
    private var original: UsuarioLocal? = null

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
        activoSwitch = findViewById(R.id.formUsuarioActivo)

        bd = BdLocal(this)
        original = intent.getStringExtra("idLocal")?.let { bd.buscar(it) }

        val anterior = original
        if (anterior == null) {
            title.text = "Nuevo usuario"
        } else {
            title.text = "Editar usuario"
            // Al editar no se cambia la contraseña: se oculta el campo y su etiqueta.
            passwordInput.visibility = View.GONE
            findViewById<TextView>(R.id.labelpassword).visibility = View.GONE
            nombreInput.setText(anterior.nombre)
            apellidoInput.setText(anterior.apellido)
            emailInput.setText(anterior.email)
            activoSwitch.isChecked = anterior.activo
        }

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { validarYGuardar() }
    }

    override fun onDestroy() {
        super.onDestroy()
        bd.close()
    }

    private fun validarYGuardar() {
        nombreInput.error = null
        apellidoInput.error = null
        emailInput.error = null
        passwordInput.error = null

        val nombre = nombreInput.text.toString().trim()
        val apellido = apellidoInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

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

            original == null && password.length < LARGO_MINIMO -> {
                passwordInput.error = "Mínimo $LARGO_MINIMO caracteres"
                passwordInput.requestFocus()
            }

            else -> guardar(nombre, apellido, email, password)
        }
    }

    private fun guardar(nombre: String, apellido: String, email: String, password: String) {
        val anterior = original
        val usuario = UsuarioLocal(
            idLocal = anterior?.idLocal ?: UUID.randomUUID().toString(),
            idRemoto = anterior?.idRemoto,
            nombre = nombre,
            apellido = apellido,
            email = email,
            password = if (anterior == null) password else anterior.password,
            activo = activoSwitch.isChecked,
            estado = when {
                anterior == null -> UsuarioLocal.PENDIENTE_CREAR
                // Si todavía no había llegado al servidor, sigue siendo un alta.
                anterior.estado == UsuarioLocal.PENDIENTE_CREAR -> UsuarioLocal.PENDIENTE_CREAR
                else -> UsuarioLocal.PENDIENTE_EDITAR
            },
            actualizado = System.currentTimeMillis(),
        )
        bd.guardar(usuario)

        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Guardado")
            .setContentText("Quedó en el teléfono; se sube a MySQL cuando haya conexión.")
            .setConfirmText("Aceptar")
            .setConfirmClickListener {
                it.dismissWithAnimation()
                finish()
            }
            .show()
    }

    private companion object {
        const val LARGO_MINIMO = 8
    }
}
