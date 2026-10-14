package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.RecoverRequest
import cl.lacteada.abstergo.dominio.Patrones
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch

class AccountRecovery : ActividadBase() {

    private lateinit var btnBack: Button
    private lateinit var btnRecover: Button
    private lateinit var emailInput: EditText
    private lateinit var recoverySignal: TextView
    private lateinit var recoveryTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_recovery)

        recoveryTitle = findViewById(R.id.titlerecovery)
        recoverySignal = findViewById(R.id.recoversignal)
        btnBack = findViewById(R.id.btnbackrecovery)
        btnRecover = findViewById(R.id.button8)
        emailInput = findViewById(R.id.correousuario)

        recoveryTitle.text = "Recupere su acceso"
        recoverySignal.text = "Ingrese correo vinculado"

        btnBack.setOnClickListener { finish() }
        btnRecover.setOnClickListener { validarYEnviar() }
    }

    private fun validarYEnviar() {
        val email = emailInput.text.toString().trim()
        emailInput.error = null

        when {
            email.isEmpty() -> {
                emailInput.error = "El correo es obligatorio"
                emailInput.requestFocus()
            }

            !Patrones.correoValido(email) -> {
                emailInput.error = "Correo inválido"
                emailInput.requestFocus()
            }

            else -> enviarCodigo(email)
        }
    }

    private fun enviarCodigo(email: String) {
        val dialogo = dialogoProgreso("Enviando código...")
        lifecycleScope.launch {
            try {
                Api.service.recover(RecoverRequest(email))
                dialogo.terminar(
                    SweetAlertDialog.SUCCESS_TYPE,
                    "¡Código enviado!",
                    "Revise su correo electrónico.",
                    boton = "Continuar",
                ) {
                    val intent = Intent(this@AccountRecovery, ConfirmationCode::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                dialogo.terminar(
                    SweetAlertDialog.ERROR_TYPE,
                    "No se pudo enviar el código",
                    "Revise su conexión e intente de nuevo.",
                )
            }
        }
    }
}
