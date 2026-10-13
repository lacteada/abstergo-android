package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import cl.lacteada.abstergo.data.Api

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<LottieAnimationView>(R.id.lottieAnimationView).playAnimation()

        this.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Handler(Looper.getMainLooper()).postDelayed({
            // Con sesión guardada entra directo al menú; si no, al login.
            val destino = if (Api.sesion.haySesion()) MainMenu::class.java
            else MainActivity::class.java
            startActivity(Intent(this, destino))
            finish()
        }, 7000) // Tiempo de espera en milisegundos
    }
}
