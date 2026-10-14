package cl.lacteada.abstergo.dominio

/**
 * El RUT chileno. Se limpia igual que en el backend (`apps/common/validators.py`)
 * y el dígito verificador se calcula por módulo 11.
 */
object Rut {

    /** Saca puntos y espacios y pasa a mayúsculas, sin agregar el guion. */
    fun limpiar(valor: String): String =
        valor.replace(".", "").replace(" ", "").uppercase()

    /** Deja el RUT en la forma canónica 12345678-9. */
    fun normalizar(valor: String): String {
        val limpio = limpiar(valor)
        if ("-" in limpio || limpio.length < 2) return limpio
        return limpio.dropLast(1) + "-" + limpio.last()
    }

    fun esValido(valor: String): Boolean {
        val partes = limpiar(valor).split("-")
        if (partes.size != 2) return false

        val cuerpo = partes[0]
        val dv = partes[1]
        if (cuerpo.isEmpty() || dv.length != 1) return false
        if (!cuerpo.all { it.isDigit() }) return false
        if (!dv[0].isDigit() && dv != "K") return false

        var suma = 0
        var factor = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += cuerpo[i].digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }

        val esperado = when (val resto = 11 - suma % 11) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }
        return dv == esperado
    }
}
