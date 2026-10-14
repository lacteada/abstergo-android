package cl.lacteada.abstergo

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * La base de todas las pantallas.
 *
 * Deja el contenido debajo de las barras del sistema y lo sube cuando aparece el
 * teclado. Antes este bloque estaba copiado en once actividades.
 */
abstract class ActividadBase : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.main)) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            vista.translationY = -teclado.bottom.toFloat()
            insets
        }
    }
}
