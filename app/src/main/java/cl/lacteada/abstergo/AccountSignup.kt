package cl.lacteada.abstergo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.RegisterRequest
import cl.lacteada.abstergo.dominio.Patrones
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** El registro de usuario. El correo es el identificador y no se puede repetir. */
class AccountSignup : ActividadBase() {

    private lateinit var title: TextView
    private lateinit var nombreInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmInput: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_signup)

        title = findViewById(R.id.titlesignup)
        nombreInput = findViewById(R.id.usersignup)
        emailInput = findViewById(R.id.correosignup)
        passwordInput = findViewById(R.id.passwordsignup)
        confirmInput = findViewById(R.id.passwordsignup2)
        btnRegister = findViewById(R.id.button)
        btnBack = findViewById(R.id.btnbacksignup)

        title.text = "Registrarse"

        btnBack.setOnClickListener { finish() }
        btnRegister.setOnClickListener { validarYRegistrar() }
    }

    private fun validarYRegistrar() {
        nombreInput.error = null
        emailInput.error = null
        passwordInput.error = null
        confirmInput.error = null

        val nombre = nombreInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val pass = passwordInput.text.toString()
        val confirm = confirmInput.text.toString()

        when {
            nombre.isEmpty() -> {
                nombreInput.error = "El nombre es obligatorio"
                nombreInput.requestFocus()
            }

            !Patrones.nombreValido(nombre) -> {
                nombreInput.error = "Solo letras y espacios"
                nombreInput.requestFocus()
            }

            !Patrones.correoValido(email) -> {
                emailInput.error = "Correo inválido"
                emailInput.requestFocus()
            }

            pass.length < LARGO_MINIMO -> {
                passwordInput.error = "Mínimo $LARGO_MINIMO caracteres"
                passwordInput.requestFocus()
            }

            pass != confirm -> {
                confirmInput.error = "Las contraseñas no coinciden"
                confirmInput.requestFocus()
            }

            else -> registrar(email, nombre, pass)
        }
    }

    private fun registrar(email: String, nombre: String, pass: String) {
        val dialogo = dialogoProgreso("Registrando...")
        lifecycleScope.launch {
            try {
                Api.service.register(RegisterRequest(email, pass, nombre))
                dialogo.terminar(
                    SweetAlertDialog.SUCCESS_TYPE,
                    "Cuenta creada",
                    "Ya puede iniciar sesión con sus credenciales.",
                ) { finish() }
            } catch (e: HttpException) {
                dialogo.terminar(
                    SweetAlertDialog.ERROR_TYPE,
                    "No se pudo registrar",
                    mensajeDeRegistro(e),
                )
            } catch (e: Exception) {
                dialogo.terminar(
                    SweetAlertDialog.ERROR_TYPE,
                    "Sin conexión",
                    "No se pudo contactar al servidor.",
                )
            }
        }
    }

    /** El servidor dice por qué rechazó el alta; hay que mostrarlo tal cual. */
    private fun mensajeDeRegistro(e: HttpException): String {
        val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
        return when {
            cuerpo.contains("ya está registrado") -> "Ese correo ya está registrado."
            e.code() == 400 -> "Revise los datos ingresados."
            else -> "El servidor respondió ${e.code()}."
        }
    }

    private companion object {
        const val LARGO_MINIMO = 8
    }
}
