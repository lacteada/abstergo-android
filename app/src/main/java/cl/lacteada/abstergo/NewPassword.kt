package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.NewPasswordRequest
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch
import retrofit2.HttpException

class NewPassword : ActividadBase() {

    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        newPassword = findViewById(R.id.newpasswordinput)
        confirmPassword = findViewById(R.id.confirmpasswordinput)
        btnSave = findViewById(R.id.btnsavepassword)
        btnBack = findViewById(R.id.btnbacknewpassword)

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { validarYGuardar() }
    }

    private fun validarYGuardar() {
        val pass = newPassword.text.toString()
        val confirm = confirmPassword.text.toString()

        newPassword.error = null
        confirmPassword.error = null

        when {
            pass.length < LARGO_MINIMO -> {
                newPassword.error = "Mínimo $LARGO_MINIMO caracteres"
                newPassword.requestFocus()
            }

            confirm.isEmpty() -> {
                confirmPassword.error = "Confirme su contraseña"
                confirmPassword.requestFocus()
            }

            pass != confirm -> {
                confirmPassword.error = "Las contraseñas no coinciden"
                confirmPassword.requestFocus()
            }

            else -> guardar(pass)
        }
    }

    private fun guardar(pass: String) {
        val email = intent.getStringExtra("email").orEmpty()
        val code = intent.getStringExtra("code").orEmpty()

        val dialogo = dialogoProgreso("Guardando...")
        lifecycleScope.launch {
            try {
                Api.service.newPassword(NewPasswordRequest(email, code, pass))
                dialogo.terminar(
                    SweetAlertDialog.SUCCESS_TYPE,
                    "¡Contraseña actualizada!",
                    "Ya puede iniciar sesión con su nueva contraseña.",
                    boton = "Ir a Login",
                ) {
                    val intent = Intent(this@NewPassword, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: HttpException) {
                dialogo.terminar(
                    SweetAlertDialog.ERROR_TYPE,
                    "No se pudo guardar",
                    "Revise los requisitos de la contraseña.",
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

    private companion object {
        const val LARGO_MINIMO = 8
    }
}
