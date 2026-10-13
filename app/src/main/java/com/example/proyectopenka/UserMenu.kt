package com.example.proyectopenka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UserMenu : AppCompatActivity() {

    private lateinit var menuTitle: TextView
    private lateinit var btnAddUser: Button
    private lateinit var btnListUsers: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        menuTitle = findViewById(R.id.user_menu_title)
        btnAddUser = findViewById(R.id.btn_add_user)
        btnListUsers = findViewById(R.id.btn_list_users)

        menuTitle.text = "Gestión de Usuarios"

        btnAddUser.setOnClickListener {
            startActivity(Intent(this, UserForm::class.java))
        }

        btnListUsers.setOnClickListener {
            startActivity(Intent(this, UserCrud::class.java))
        }
    }
}
