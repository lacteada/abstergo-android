package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.RecuperarRequest
import kotlinx.coroutines.launch

class AccountRecovery : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnRecover: Button
    private lateinit var emailInput: EditText
    private lateinit var recoverysignal: TextView
    private lateinit var recoverytitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_recovery)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        recoverytitle = findViewById(R.id.titlerecovery)
        recoverysignal = findViewById(R.id.recoversignal)
        btnBack = findViewById(R.id.btnbackrecovery)
        btnRecover = findViewById(R.id.button8)
        emailInput = findViewById(R.id.correousuario)

        recoverytitle.setText("Recupere su acceso")
        recoverysignal.setText("Ingrese correo vinculado")

        btnBack.setOnClickListener {
            finish()
        }

        btnRecover.setOnClickListener {
            val email = emailInput.text.toString().trim()
            emailInput.error = null

            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

            when {
                email.isEmpty() -> {
                    emailInput.error = "El correo es obligatorio"
                    emailInput.requestFocus()
                }
                !emailRegex.matches(email) -> {
                    emailInput.error = "Correo inválido"
                    emailInput.requestFocus()
                }
                else -> enviarCodigo(email)
            }
        }
    }

    private fun enviarCodigo(email: String) {
        val dialogo = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        dialogo.titleText = "Enviando código..."
        dialogo.setCancelable(false)
        dialogo.show()

        lifecycleScope.launch {
            try {
                Api.service.recuperar(RecuperarRequest(email))

                dialogo.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                dialogo.titleText = "¡Código enviado!"
                dialogo.contentText = "Revise su correo electrónico."
                dialogo.confirmText = "Continuar"
                dialogo.setConfirmClickListener {
                    it.dismissWithAnimation()
                    val intent = Intent(this@AccountRecovery, ConfirmationCode::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                dialogo.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                dialogo.titleText = "No se pudo enviar el código"
                dialogo.contentText = "Revise su conexión e intente de nuevo."
                dialogo.confirmText = "Aceptar"
                dialogo.setConfirmClickListener { it.dismissWithAnimation() }
            }
        }
    }
}
