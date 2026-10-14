package cl.lacteada.abstergo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import cl.lacteada.abstergo.data.Api
import com.airbnb.lottie.LottieAnimationView

class Splash : ActividadBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )
        findViewById<LottieAnimationView>(R.id.lottieAnimationView).playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            // Con sesión guardada entra directo al menú; si no, al login.
            val destino = if (Api.sesion.haySesion()) MainMenu::class.java
            else MainActivity::class.java
            startActivity(Intent(this, destino))
            finish()
        }, DEMORA_MS)
    }

    private companion object {
        const val DEMORA_MS = 7000L
    }
}
