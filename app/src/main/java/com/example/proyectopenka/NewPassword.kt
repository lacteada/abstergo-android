package com.example.proyectopenka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.proyectopenka.data.Api
import com.example.proyectopenka.data.NuevaPasswordRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class NewPassword : AppCompatActivity() {

    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        newPassword = findViewById(R.id.newpasswordinput)
        confirmPassword = findViewById(R.id.confirmpasswordinput)
        btnSave = findViewById(R.id.btnsavepassword)
        btnBack = findViewById(R.id.btnbacknewpassword)

        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            val pass = newPassword.text.toString().trim()
            val confirm = confirmPassword.text.toString().trim()

            newPassword.error = null
            confirmPassword.error = null

            when {
                pass.isEmpty() -> {
                    newPassword.error = "La contraseña es obligatoria"
                    newPassword.requestFocus()
                }
                pass.length < 8 -> {
                    newPassword.error = "Mínimo 8 caracteres"
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
    }

    private fun guardar(pass: String) {
        val email = intent.getStringExtra("email") ?: ""
        val codigo = intent.getStringExtra("codigo") ?: ""

        val dialogo = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        dialogo.titleText = "Guardando..."
        dialogo.setCancelable(false)
        dialogo.show()

        lifecycleScope.launch {
            try {
                Api.service.nuevaPassword(NuevaPasswordRequest(email, codigo, pass))

                dialogo.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                dialogo.titleText = "¡Contraseña actualizada!"
                dialogo.contentText = "Ya puede iniciar sesión con su nueva contraseña."
                dialogo.confirmText = "Ir a Login"
                dialogo.setConfirmClickListener {
                    it.dismissWithAnimation()
                    val intent = Intent(this@NewPassword, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: HttpException) {
                dialogo.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                dialogo.titleText = "No se pudo guardar"
                dialogo.contentText = "Revise los requisitos de la contraseña."
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
}
