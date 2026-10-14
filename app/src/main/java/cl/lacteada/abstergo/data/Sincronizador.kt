package cl.lacteada.abstergo.data

import android.content.Context

/**
 * Sube a MySQL lo que quedó pendiente en la base local.
 *
 * Los cambios se marcan como sincronizados **solo** cuando el servidor
 * confirma. Si algo falla, la fila se queda pendiente y se reintenta después.
 * Al terminar, si no queda nada pendiente, se trae la lista del servidor.
 */
object Sincronizador {

    data class Resultado(
        val sincronizados: Int,
        val pendientes: Int,
        val trajoDelServidor: Boolean = false,
        val error: String? = null,
    )

    suspend fun sincronizar(context: Context, bd: BdLocal): Resultado {
        if (!Conectividad.hayRed(context)) {
            return Resultado(0, bd.cantidadPendientes(), error = "Sin conexión.")
        }

        var hechos = 0
        for (usuario in bd.pendientes()) {
            try {
                when (usuario.estado) {
                    UsuarioLocal.PENDIENTE_BORRAR -> {
                        usuario.idRemoto?.let { Api.service.deleteUser(it) }
                        bd.eliminar(usuario.idLocal)
                    }

                    UsuarioLocal.PENDIENTE_EDITAR -> {
                        val idRemoto = usuario.idRemoto
                            ?: error("Un cambio sin id remoto no se puede editar.")
                        Api.service.updateUser(idRemoto, usuario.aRemoto())
                        bd.marcarSincronizado(usuario.idLocal, idRemoto)
                    }

                    else -> {
                        val creado = Api.service.createUser(usuario.aRemoto())
                        bd.marcarSincronizado(usuario.idLocal, creado.id ?: error("Sin id."))
                    }
                }
                hechos++
            } catch (e: Exception) {
                // Se queda pendiente: se reintenta en el próximo intento.
                return Resultado(hechos, bd.cantidadPendientes(), error = e.message)
            }
        }

        // Nada pendiente: la copia local se alinea con el servidor.
        var trajo = false
        if (bd.cantidadPendientes() == 0) {
            val remotos = Api.service.users().results.map { UsuarioLocal.desdeRemoto(it) }
            bd.reemplazarSincronizados(remotos)
            trajo = true
        }
        return Resultado(hechos, bd.cantidadPendientes(), trajoDelServidor = trajo)
    }
}
