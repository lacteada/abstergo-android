package cl.lacteada.abstergo.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

/** Si hay Internet, por Wi-Fi o por datos móviles. */
object Conectividad {

    fun hayRed(context: Context): Boolean {
        val gestor = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val red = gestor.activeNetwork ?: return false
        val capacidades = gestor.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Avisa cuando la red aparece o se cae. */
    fun observar(context: Context, alCambiar: (Boolean) -> Unit): ConnectivityManager.NetworkCallback? {
        val gestor = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return null
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(red: Network) {
                alCambiar(true)
            }

            override fun onLost(red: Network) {
                alCambiar(hayRed(context))
            }
        }
        return runCatching {
            gestor.registerDefaultNetworkCallback(callback)
            callback
        }.getOrNull()
    }

    fun dejarDeObservar(context: Context, callback: ConnectivityManager.NetworkCallback?) {
        if (callback == null) return
        val gestor = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        runCatching { gestor?.unregisterNetworkCallback(callback) }
    }
}
