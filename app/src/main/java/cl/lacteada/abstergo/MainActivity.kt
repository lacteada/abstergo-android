package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.LoginRequest
import cl.lacteada.abstergo.dominio.Patrones
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainActivity : ActividadBase() {

    private lateinit var btnLogin: Button
    private lateinit var btnForgot: Button
    private lateinit var loginTitle: TextView
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.btnlogintry)
        btnForgot = findViewById(R.id.btnprogreso)
        loginTitle = findViewById(R.id.logintitle)
        loginEmail = findViewById(R.id.correologin)
        loginPassword = findViewById(R.id.passwordlogin)
        loginText = findViewById(R.id.loginsignal)

        loginTitle.text = "Abstergo"
        loginText.text = "Ingrese sus credenciales"

        btnForgot.setOnClickListener {
            startActivity(Intent(this, AccountRecovery::class.java))
        }
        btnLogin.setOnClickListener { validarYEntrar() }
    }

    private fun validarYEntrar() {
        val email = loginEmail.text.toString().trim()
        val clave = loginPassword.text.toString()

        loginEmail.error = null
        loginPassword.error = null

        when {
            email.isEmpty() -> {
                loginEmail.error = "El correo es obligatorio"
                loginEmail.requestFocus()
            }

            !Patrones.correoValido(email) -> {
                loginEmail.error = "Correo inválido"
                loginEmail.requestFocus()
            }

            clave.isEmpty() -> {
                loginPassword.error = "La contraseña es obligatoria"
                loginPassword.requestFocus()
            }

            else -> iniciarSesion(email, clave)
        }
    }

    private fun iniciarSesion(email: String, clave: String) {
        val dialogo = dialogoProgreso("Iniciando sesión...")
        lifecycleScope.launch {
            try {
                val respuesta = Api.service.login(LoginRequest(email, clave))
                Api.sesion.guardar(respuesta.access, respuesta.refresh)
                dialogo.terminar(
                    SweetAlertDialog.SUCCESS_TYPE,
                    "¡Bienvenido!",
                    "Sesión iniciada correctamente.",
                ) {
                    startActivity(Intent(this@MainActivity, MainMenu::class.java))
                    finish()
                }
            } catch (e: HttpException) {
                dialogo.terminar(
                    SweetAlertDialog.ERROR_TYPE,
                    "No se pudo iniciar sesión",
                    if (e.code() == 401) {
                        "Correo o contraseña incorrectos."
                    } else {
                        "El servidor respondió ${e.code()}."
                    },
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
}
