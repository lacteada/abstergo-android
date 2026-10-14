package cl.lacteada.abstergo.dominio

import java.text.Normalizer

/** Para comparar texto sin distinguir mayúsculas ni acentos. */
fun sinAcentos(valor: String): String =
    Normalizer.normalize(valor, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
