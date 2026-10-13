package com.example.proyectopenka

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

class AccountSignup : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnCreate: Button
    private lateinit var emailInput: EditText
    private lateinit var userInput: EditText
    private lateinit var passInput: EditText
    private lateinit var passConfirmInput: EditText
    private lateinit var titlesignup: TextView
    private lateinit var signupsignal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }
        titlesignup = findViewById(R.id.titlesignup)
        btnBack = findViewById(R.id.btnbacksignup)
        btnCreate = findViewById(R.id.button)
        emailInput = findViewById(R.id.correosignup)
        userInput = findViewById(R.id.usersignup)
        passInput = findViewById(R.id.passwordsignup)
        passConfirmInput = findViewById(R.id.passwordsignup2)

        titlesignup.setText("Cree su cuenta")

        btnBack.setOnClickListener { finish() }

        btnCreate.setOnClickListener {
            emailInput.error = null
            userInput.error = null
            passInput.error = null
            passConfirmInput.error = null

            val email = emailInput.text.toString().trim()
            val user = userInput.text.toString().trim()
            val pass = passInput.text.toString().trim()
            val passConfirm = passConfirmInput.text.toString().trim()

            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
            val passRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&#]).{8,}$")

            var hasError = false

            if (email.isEmpty()) {
                emailInput.error = "El correo es obligatorio"
                hasError = true
            } else if (!emailRegex.matches(email)) {
                emailInput.error = "Correo inválido"
                hasError = true
            }

            if (user.isEmpty()) {
                userInput.error = "El usuario es obligatorio"
                hasError = true
            } else if (user.length < 3) {
                userInput.error = "Mínimo 3 caracteres"
                hasError = true
            }

            if (pass.isEmpty()) {
                passInput.error = "La contraseña es obligatoria"
                hasError = true
            } else if (pass.length < 8) {
                passInput.error = "Mínimo 8 caracteres"
                hasError = true
            } else if (!passRegex.matches(pass)) {
                passInput.error = "Debe tener una mayúscula, una minúscula, un número y un carácter especial"
                hasError = true
            }

            if (passConfirm.isEmpty()) {
                passConfirmInput.error = "Confirme su contraseña"
                hasError = true
            } else if (pass != passConfirm) {
                passConfirmInput.error = "Las contraseñas no coinciden"
                hasError = true
            }

            if (hasError) return@setOnClickListener

            val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            dialog.titleText = "Creando cuenta..."
            dialog.setCancelable(false)
            dialog.show()

            Handler(Looper.getMainLooper()).postDelayed({
                dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                dialog.titleText = "Error de conexión"
                dialog.contentText = "No se pudo conectar al servidor."
                dialog.confirmText = "Aceptar"
                dialog.setConfirmClickListener { it.dismissWithAnimation() }
            }, 2000)
        }
    }
}
