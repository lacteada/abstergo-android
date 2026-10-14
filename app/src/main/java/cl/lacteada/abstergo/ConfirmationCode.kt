package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cl.lacteada.abstergo.data.Api
import cl.lacteada.abstergo.data.RecoverRequest
import cl.lacteada.abstergo.data.VerifyRequest
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ConfirmationCode : ActividadBase() {

    private lateinit var digits: List<EditText>
    private lateinit var btnBack: Button
    private lateinit var btnVerify: Button
    private lateinit var btnReenviar: Button
    private lateinit var txtContador: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var segundos = SEGUNDOS_VIGENCIA

    /** Cuenta regresiva del minuto de vigencia del código. */
    private val contador = object : Runnable {
        override fun run() {
            segundos--
            if (segundos > 0) {
                txtContador.text = "El código vence en ${segundos}s"
                handler.postDelayed(this, MILISEGUNDOS)
            } else {
                txtContador.text = "El código venció. Pida uno nuevo."
                btnReenviar.isEnabled = true
            }
        }
    }

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
        btnReenviar = findViewById(R.id.btnReenviar)
        txtContador = findViewById(R.id.txtContador)

        btnBack.setOnClickListener { finish() }
        btnVerify.setOnClickListener { verificarCodigo() }
        btnReenviar.setOnClickListener { reenviar() }

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

            // Retroceso en un campo vacío: borra el anterior y vuelve a él,
            // así se puede borrar el código completo sin tocar campo por campo.
            campo.setOnKeyListener { _, tecla, evento ->
                if (tecla == KeyEvent.KEYCODE_DEL &&
                    evento.action == KeyEvent.ACTION_DOWN &&
                    campo.text.isEmpty() &&
                    indice > 0
                ) {
                    digits[indice - 1].setText("")
                    digits[indice - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }

        iniciarContador()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(contador)
    }

    private fun iniciarContador() {
        handler.removeCallbacks(contador)
        segundos = SEGUNDOS_VIGENCIA
        btnReenviar.isEnabled = false
        txtContador.text = "El código vence en ${segundos}s"
        handler.postDelayed(contador, MILISEGUNDOS)
    }

    private fun verificarCodigo() {
        val email = intent.getStringExtra("email").orEmpty()
        val code = digits.joinToString("") { it.text.toString().trim() }

        if (code.length < digits.size) {
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

    private fun reenviar() {
        val email = intent.getStringExtra("email").orEmpty()
        val dialogo = dialogoProgreso("Enviando código...")
        lifecycleScope.launch {
            try {
                Api.service.recover(RecoverRequest(email))
                dialogo.terminar(
                    SweetAlertDialog.SUCCESS_TYPE,
                    "Código reenviado",
                    "Revise su correo electrónico.",
                ) { iniciarContador() }
            } catch (e: Exception) {
                dialogo.terminar(
                    SweetAlertDialog.ERROR_TYPE,
                    "No se pudo reenviar",
                    "Revise su conexión e intente de nuevo.",
                )
            }
        }
    }

    private companion object {
        const val SEGUNDOS_VIGENCIA = 60
        const val MILISEGUNDOS = 1_000L
    }
}
