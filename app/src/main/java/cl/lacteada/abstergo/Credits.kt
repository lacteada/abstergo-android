package cl.lacteada.abstergo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

/**
 * Los datos del desarrollador. Vista de solo lectura.
 *
 * Los textos están para completar: pon los datos reales acá y en el layout.
 */
class Credits : ActividadBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)

        findViewById<TextView>(R.id.txtUsername).text = NOMBRE
        findViewById<TextView>(R.id.txtRol).text = ROL
        findViewById<TextView>(R.id.txtMail).text = CORREO
        findViewById<TextView>(R.id.txtCarrera).text = CARRERA

        findViewById<Button>(R.id.btnbackcredits).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnGithub).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB)))
        }
    }

    private companion object {
        const val NOMBRE = "Sebastian Gilberto"
        const val ROL = "Rol: Full-stack"
        const val CORREO = "Mail: sebastian.gilberto02@inacapmail.cl"
        const val CARRERA = "Ing. Ciberseguridad | INACAP La Serena | P1-C1"
        const val GITHUB = "https://github.com/lacteada"
    }
}
