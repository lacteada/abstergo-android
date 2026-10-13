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
import cl.lacteada.abstergo.data.LoginRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {

    private lateinit var btnlogin: Button
    private lateinit var btnforgot: Button
    private lateinit var logintitle: TextView
    private lateinit var loginemail: EditText
    private lateinit var loginclave: EditText
    private lateinit var logintext: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        btnlogin = findViewById(R.id.btnlogintry)
        btnforgot = findViewById(R.id.btnprogreso)
        logintitle = findViewById(R.id.logintitle)
        loginemail = findViewById(R.id.correologin)
        loginclave = findViewById(R.id.passwordlogin)
        logintext = findViewById(R.id.loginsignal)

        logintitle.setText("Abstergo")
        logintext.setText("Ingrese sus credenciales")

        btnforgot.setOnClickListener {
            startActivity(Intent(this, AccountRecovery::class.java))
        }

        btnlogin.setOnClickListener {
            val email = loginemail.text.toString().trim()
            val clave = loginclave.text.toString()

            loginemail.error = null
            loginclave.error = null

            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

            when {
                email.isEmpty() -> {
                    loginemail.error = "El correo es obligatorio"
                    loginemail.requestFocus()
                }

                !emailRegex.matches(email) -> {
                    loginemail.error = "Correo inválido"
                    loginemail.requestFocus()
                }

                clave.isEmpty() -> {
                    loginclave.error = "La contraseña es obligatoria"
                    loginclave.requestFocus()
                }

                else -> iniciarSesion(email, clave)
            }
        }
    }

    private fun iniciarSesion(email: String, clave: String) {
        val dialogo = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        dialogo.titleText = "Iniciando sesión..."
        dialogo.setCancelable(false)
        dialogo.show()

        lifecycleScope.launch {
            try {
                val respuesta = Api.service.login(LoginRequest(email, clave))
                Api.sesion.guardar(respuesta.access, respuesta.refresh)

                dialogo.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                dialogo.titleText = "¡Bienvenido!"
                dialogo.contentText = "Sesión iniciada correctamente."
                dialogo.confirmText = "Aceptar"
                dialogo.setConfirmClickListener {
                    it.dismissWithAnimation()
                    startActivity(Intent(this@MainActivity, MainMenu::class.java))
                    finish()
                }
            } catch (e: HttpException) {
                dialogo.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                dialogo.titleText = "No se pudo iniciar sesión"
                dialogo.contentText = if (e.code() == 401) {
                    "Correo o contraseña incorrectos."
                } else {
                    "El servidor respondió ${e.code()}."
                }
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
