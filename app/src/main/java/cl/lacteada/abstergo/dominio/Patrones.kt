package cl.lacteada.abstergo.dominio

/** Los patrones de entrada que comparten las pantallas. */
object Patrones {

    private val CORREO = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    // Solo letras y espacios, con tildes y ñ, como pide la pauta.
    private val NOMBRE = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?: [A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$")

    fun correoValido(valor: String): Boolean = CORREO.matches(valor)

    fun nombreValido(valor: String): Boolean = NOMBRE.matches(valor.trim())
}
