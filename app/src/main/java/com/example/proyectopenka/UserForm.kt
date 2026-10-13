package com.example.proyectopenka

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

class UserForm : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var title: TextView
    private lateinit var rutInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var nombreInput: EditText
    private lateinit var apellidoInput: EditText
    private lateinit var passInput: EditText
    private lateinit var passConfirmInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        btnBack = findViewById(R.id.btnbackform)
        btnSave = findViewById(R.id.btnsaveform)
        title = findViewById(R.id.titleform)

        rutInput = findViewById(R.id.formRut)
        emailInput = findViewById(R.id.formEmail)
        nombreInput = findViewById(R.id.formNombre)
        apellidoInput = findViewById(R.id.formApellido)
        passInput = findViewById(R.id.formPass)
        passConfirmInput = findViewById(R.id.formPassConfirm)

        val isEdit = intent.getBooleanExtra("isEdit", false)

        if (isEdit) {
            title.text = "Editar usuario"
            rutInput.setText(intent.getStringExtra("rut") ?: "")
            emailInput.setText(intent.getStringExtra("email") ?: "")
            nombreInput.setText(intent.getStringExtra("nombre") ?: "")
            apellidoInput.setText(intent.getStringExtra("apellido") ?: "")
        }

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            rutInput.error = null
            emailInput.error = null
            nombreInput.error = null
            apellidoInput.error = null
            passInput.error = null
            passConfirmInput.error = null

            val rut = rutInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val nombre = nombreInput.text.toString().trim()
            val apellido = apellidoInput.text.toString().trim()
            val pass = passInput.text.toString().trim()
            val passConfirm = passConfirmInput.text.toString().trim()

            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
            val passRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&#]).{8,}$")

            var hasError = false

            if (rut.isEmpty()) {
                rutInput.error = "El RUT es obligatorio"
                hasError = true
            } else if (!isValidRut(rut)) {
                rutInput.error = "RUT inválido (ej: 12345678-9)"
                hasError = true
            }

            if (email.isEmpty()) {
                emailInput.error = "El correo es obligatorio"
                hasError = true
            } else if (!emailRegex.matches(email)) {
                emailInput.error = "Correo inválido"
                hasError = true
            }

            if (nombre.isEmpty()) {
                nombreInput.error = "El nombre es obligatorio"
                hasError = true
            } else if (nombre.length < 2) {
                nombreInput.error = "Mínimo 2 caracteres"
                hasError = true
            }

            if (apellido.isEmpty()) {
                apellidoInput.error = "El apellido es obligatorio"
                hasError = true
            } else if (apellido.length < 2) {
                apellidoInput.error = "Mínimo 2 caracteres"
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
            dialog.titleText = "Guardando..."
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

    private fun isValidRut(rut: String): Boolean {
        val clean = rut.replace(".", "").uppercase()
        val parts = clean.split("-")
        if (parts.size != 2) return false

        val body = parts[0]
        val dv = parts[1]
        if (body.isEmpty() || dv.isEmpty()) return false
        if (!body.all { it.isDigit() }) return false
        if (dv.length != 1 || (!dv[0].isDigit() && dv != "K")) return false

        var sum = 0
        var factor = 2
        for (i in body.length - 1 downTo 0) {
            sum += body[i].digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }

        val remainder = sum % 11
        val calculated = when (val result = 11 - remainder) {
            11 -> "0"
            10 -> "K"
            else -> result.toString()
        }

        return dv == calculated
    }
}
