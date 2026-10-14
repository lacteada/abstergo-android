package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.VerifyRequest
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ConfirmationCode : ActividadBase() {

    private lateinit var digits: List<EditText>
    private lateinit var btnBack: Button
    private lateinit var btnVerify: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation_code)

        digits = listOf(
            findViewById(R.id.digit1),
            findViewById(R.id.digit2),
            findViewById(R.id.digit3),
            findViewById(R.id.digit4),
            findViewById(R.id.digit5),
            findViewById(R.id.digit6),
        )

        btnBack = findViewById(R.id.btnbackconfirmation)
        btnVerify = findViewById(R.id.btnverifycode)

        btnBack.setOnClickListener { finish() }
        btnVerify.setOnClickListener { verificarCodigo() }

        digits.forEachIndexed { indice, campo ->
            campo.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                    if (s?.length == 1 && indice < digits.size - 1) {
                        digits[indice + 1].requestFocus()
                    }
                }
            })
        }
    }

    private fun verificarCodigo() {
        val email = intent.getStringExtra("email").orEmpty()
        val code = digits.joinToString("") { it.text.toString().trim() }

        if (code.length < 6) {
            aviso("Código incompleto", "Ingrese los 6 dígitos.", SweetAlertDialog.WARNING_TYPE)
            return
        }
        verificar(email, code)
    }

    private fun verificar(email: String, code: String) {
        val dialogo = dialogoProgreso("Verificando...")
        lifecycleScope.launch {
            try {
                Api.service.verify(VerifyRequest(email, code))
                dialogo.terminar(
                    SweetAlertDialog.SUCCESS_TYPE,
                    "¡Código correcto!",
                    "Ahora puede crear una nueva contraseña.",
                    boton = "Continuar",
                ) {
                    val intent = Intent(this@ConfirmationCode, NewPassword::class.java)
                    intent.putExtra("email", email)
                    intent.putExtra("code", code)
                    startActivity(intent)
                    finish()
                }
            } catch (e: HttpException) {
                dialogo.terminar(
                    SweetAlertDialog.ERROR_TYPE,
                    "Código incorrecto",
                    "El código es inválido o ya venció.",
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
