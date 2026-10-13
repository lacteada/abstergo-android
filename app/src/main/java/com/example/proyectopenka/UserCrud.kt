package com.example.proyectopenka

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

class UserCrud : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var searchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_crud)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.translationY = -ime.bottom.toFloat()
            insets
        }

        btnBack = findViewById(R.id.btnbackcrud)
        searchInput = findViewById(R.id.searchInput)

        btnBack.setOnClickListener { finish() }

        val users = listOf(
            findViewById<TextView>(R.id.user1),
            findViewById<TextView>(R.id.user2),
            findViewById<TextView>(R.id.user3)
        )

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                for (user in users) {
                    val matches = user.text.toString().lowercase().contains(query)
                    user.visibility = if (matches) View.VISIBLE else View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        for (user in users) {
            user.setOnClickListener {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(user.text.toString())
                    .setContentText("¿Qué desea hacer?")
                    .setConfirmText("Editar")
                    .setCancelText("Eliminar")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        val intent = Intent(this, UserForm::class.java)
                        intent.putExtra("isEdit", true)
                        intent.putExtra("rut", "12.345.678-9")
                        intent.putExtra("email", user.text.toString().split(" - ").getOrNull(1) ?: "")
                        intent.putExtra("nombre", user.text.toString().split(" - ").getOrNull(0)?.split(" ")?.getOrNull(0) ?: "")
                        intent.putExtra("apellido", user.text.toString().split(" - ").getOrNull(0)?.split(" ")?.getOrNull(1) ?: "")
                        startActivity(intent)
                    }
                    .setCancelClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Eliminar usuario")
                            .setContentText("No se pudo conectar al servidor.")
                            .setConfirmText("Aceptar")
                            .setConfirmClickListener { it.dismissWithAnimation() }
                            .show()
                    }
                    .show()
            }
        }
    }
}
