package cl.lacteada.abstergo

import cl.lacteada.abstergo.data.Api

/**
 * Lo que el usuario puede hacer, según lo que devuelve `/auth/me/`.
 * La matriz vive en el backend; acá solo se consulta.
 */
object Permisos {

    fun puedeLeer(modulo: String): Boolean =
        Api.perfil?.modulosLectura?.contains(modulo) == true

    fun puedeEscribir(modulo: String): Boolean =
        Api.perfil?.modulosEscritura?.contains(modulo) == true
}
