package cl.lacteada.abstergo.data

/** Un usuario tal como vive en la base local, con su estado de sincronización. */
data class UsuarioLocal(
    val idLocal: String,
    val idRemoto: Int?,
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String?,
    val activo: Boolean,
    val estado: String,
    val actualizado: Long,
) {

    /** ¿Tiene cambios que todavía no llegaron al servidor? */
    val pendiente: Boolean get() = estado != SINCRONIZADO

    /** Cómo se manda a la API. */
    fun aRemoto() = User(
        id = idRemoto,
        email = email,
        firstName = nombre,
        lastName = apellido,
        active = activo,
        password = password,
    )

    companion object {
        const val PENDIENTE_CREAR = "pendiente_crear"
        const val PENDIENTE_EDITAR = "pendiente_editar"
        const val PENDIENTE_BORRAR = "pendiente_borrar"
        const val SINCRONIZADO = "sincronizado"

        /** Los que ya vienen del servidor llevan un id local estable, para no duplicar. */
        fun desdeRemoto(remoto: User) = UsuarioLocal(
            idLocal = "remoto-${remoto.id}",
            idRemoto = remoto.id,
            nombre = remoto.firstName,
            apellido = remoto.lastName,
            email = remoto.email.orEmpty(),
            password = null,
            activo = remoto.active,
            estado = SINCRONIZADO,
            actualizado = System.currentTimeMillis(),
        )
    }
}
