package cl.lacteada.abstergo.dominio

/** Los patrones de entrada que comparten las pantallas. */
object Patrones {

    private val CORREO = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    fun correoValido(valor: String): Boolean = CORREO.matches(valor)
}
