package cl.lacteada.abstergo.dominio

/** Los valores que el backend acepta para cada campo de la atención. */
object Catalogos {

    val ESTADOS = listOf("ingresada", "en_proceso", "cerrada", "derivada")

    val CANALES = listOf("presencial", "telefono", "web")
}
