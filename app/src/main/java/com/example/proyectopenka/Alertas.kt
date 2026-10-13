package com.example.proyectopenka

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

class Alertas : AppCompatActivity() {

    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btn4: Button
    private lateinit var btn5: Button
    private lateinit var btn6: Button
    private lateinit var btnlogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alertas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn1 = findViewById(R.id.btnadvertencia)
        btn2 = findViewById(R.id.btnexito)
        btn3 = findViewById(R.id.btnerror)
        btn4 = findViewById(R.id.btnprogreso)
        btn5 = findViewById(R.id.btnconfirmacion)
        btn6 = findViewById(R.id.btnprogresoexito)

        btnlogin = findViewById(R.id.btnlogin)

        btnlogin.setOnClickListener {
            val intent = Intent(this, MainActivity ::class.java)
            startActivity(intent)
        }

        btn1.setOnClickListener {
            mostrarAdvertencia()
        }
        btn2.setOnClickListener{
            mostrarExito()
        }
        btn3.setOnClickListener{
            mostrarError()
        }
        btn4.setOnClickListener{
            mostrarCargando()
        }
        btn5.setOnClickListener{
            mostrarConfirmacion()
        }
        btn6.setOnClickListener{
            procesoConCargandoYExito()
        }
    }

    private fun mostrarAdvertencia() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Advertencia")
            .setContentText("Este cambio no se puede deshacer.")
            .setConfirmText("Entiendo")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun mostrarExito() {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("¡Éxito!")
            .setContentText("La operación fue realizada correctamente.")
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun mostrarError() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Algo salió mal. Intenta nuevamente.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun mostrarCargando()
    {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.dismissWithAnimation()
        }, 3000) // 3000 milisegundos = 3 segundos
    }

    private fun mostrarConfirmacion() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Estás seguro?")
            .setContentText("Esta acción eliminará los datos.")
            .setConfirmText("Sí")
            .setCancelText("No")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                mostrarExito() // Aquí podrías ejecutar la acción real
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun procesoConCargandoYExito() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()
        // Simular una operación (por ejemplo, guardar datos)
        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
            pDialog.titleText = "¡Éxito!"
            pDialog.contentText = "La operación se completó correctamente."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
        }, 3000) // Espera 3 segundos simulando un proceso
    }
}
