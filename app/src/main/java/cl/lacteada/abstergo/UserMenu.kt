package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

/** El menú de gestión: ingresar un usuario o listarlos. */
class UserMenu : ActividadBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_menu)

        findViewById<TextView>(R.id.user_menu_title).text = "Gestión de Usuarios"
        findViewById<TextView>(R.id.user_menu_title2).text = "Ingresar o listar"

        // "Ingresar usuario" abre el formulario de registro.
        findViewById<Button>(R.id.btn_add_user).setOnClickListener {
            startActivity(Intent(this, AccountSignup::class.java))
        }
        findViewById<Button>(R.id.btn_list_users).setOnClickListener {
            startActivity(Intent(this, UsuarioCrud::class.java))
        }
    }
}
