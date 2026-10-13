package com.example.proyectopenka

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.proyectopenka.data.Api
import com.example.proyectopenka.data.ValidarRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ConfirmationCode : AppCompatActivity() {

    private lateinit var digits: List<EditText>
    private lateinit var btnBack: Button
    private lateinit var btnVerify: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmation_code)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        digits = listOf(
            findViewById(R.id.digit1),
            findViewById(R.id.digit2),
            findViewById(R.id.digit3),
            findViewById(R.id.digit4),
            findViewById(R.id.digit5),
            findViewById(R.id.digit6)
        )

        btnBack = findViewById(R.id.btnbackconfirmation)
        btnVerify = findViewById(R.id.btnverifycode)

        btnBack.setOnClickListener {
            finish()
        }

        setupDigitAutoAdvance()

        btnVerify.setOnClickListener {
            val email = intent.getStringExtra("email") ?: ""
            val code = digits.joinToString("") { it.text.toString().trim() }

            if (code.length < 6) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Código incompleto")
                    .setContentText("Ingrese los 6 dígitos.")
                    .setConfirmText("Entiendo")
                    .setConfirmClickListener { it.dismissWithAnimation() }
                    .show()
                return@setOnClickListener
            }

            verificar(email, code)
        }
    }

    private fun verificar(email: String, codigo: String) {
        val dialogo = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        dialogo.titleText = "Verificando..."
        dialogo.setCancelable(false)
        dialogo.show()

        lifecycleScope.launch {
            try {
                Api.service.validar(ValidarRequest(email, codigo))

                dialogo.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                dialogo.titleText = "¡Código correcto!"
                dialogo.contentText = "Ahora puede crear una nueva contraseña."
                dialogo.confirmText = "Continuar"
                dialogo.setConfirmClickListener {
                    it.dismissWithAnimation()
                    val intent = Intent(this@ConfirmationCode, NewPassword::class.java)
                    intent.putExtra("email", email)
                    intent.putExtra("codigo", codigo)
                    startActivity(intent)
                    finish()
                }
            } catch (e: HttpException) {
                dialogo.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                dialogo.titleText = "Código incorrecto"
                dialogo.contentText = "El código es inválido o ya venció."
                dialogo.confirmText = "Aceptar"
                dialogo.setConfirmClickListener { it.dismissWithAnimation() }
            } catch (e: Exception) {
                dialogo.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                dialogo.titleText = "Sin conexión"
                dialogo.contentText = "No se pudo contactar al servidor."
                dialogo.confirmText = "Aceptar"
                dialogo.setConfirmClickListener { it.dismissWithAnimation() }
            }
        }
    }

    private fun setupDigitAutoAdvance() {
        for (i in digits.indices) {
            digits[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < digits.size - 1) {
                        digits[i + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}
