package cl.lacteada.abstergo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import cl.lacteada.abstergo.data.Api

/**
 * Si el refresco del token falló, la sesión quedó vacía: vuelve al login.
 * Devuelve true cuando redirigió, para cortar el flujo que la llamó.
 */
fun AppCompatActivity.irALoginSiExpiro(): Boolean {
    if (Api.sesion.haySesion()) return false
    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
    finish()
    return true
}
