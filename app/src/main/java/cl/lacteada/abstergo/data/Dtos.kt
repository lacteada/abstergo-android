package cl.lacteada.abstergo.data

import com.google.gson.annotations.SerializedName

/*
 * Los nombres y los campos siguen el contrato de la API, que está en inglés
 * (ver `documento_tecnico.md` §18.3). Una sola clase sirve para leer y para
 * escribir: el `id` y los campos de solo lectura van en nulo al crear, y Gson
 * no serializa los nulos.
 */

// ------------------------------------------------------------- Autenticación
data class LoginRequest(val email: String, val password: String)

data class RefreshRequest(val refresh: String)

data class TokenResponse(val access: String, val refresh: String? = null)

data class Message(val detail: String)

data class RecoverRequest(val email: String)

data class VerifyRequest(val email: String, val code: String)

data class NewPasswordRequest(val email: String, val code: String, val password: String)

/** El alta pública: el correo es el identificador. */
data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("first_name") val firstName: String = "",
)

// ------------------------------------------------------------------ Comunes
data class Page<T>(val count: Int, val results: List<T>)

// ----------------------------------------------------------------- Entidades
data class Role(val id: Int, val code: String?, val name: String, val description: String?)

data class User(
    val id: Int? = null,
    val email: String? = null,
    @SerializedName("first_name") val firstName: String = "",
    @SerializedName("last_name") val lastName: String = "",
    val role: Int? = null,
    val delegation: Int? = null,
    @SerializedName("is_active") val active: Boolean = true,
    val password: String? = null,
)

/**
 * Lo que devuelve `/auth/me/`: el usuario más sus módulos.
 *
 * Gson no deserializa una jerarquía de data classes, así que `Profile` repite
 * los campos de `User` a propósito.
 */
data class Profile(
    val id: Int,
    val email: String?,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val role: Int?,
    val delegation: Int?,
    @SerializedName("is_active") val active: Boolean,
    @SerializedName("role_code") val roleCode: String? = null,
    @SerializedName("read_modules") val readModules: List<String> = emptyList(),
    @SerializedName("write_modules") val writeModules: List<String> = emptyList(),
)

data class Delegation(
    val id: Int = 0,
    val code: String = "",
    val name: String,
    val address: String = "",
    val commune: String = "",
    val manager: Int? = null,
)

data class AttentionType(val id: Int, val name: String, val description: String? = null)

data class AttentionSubtype(
    val id: Int,
    val name: String,
    @SerializedName("attention_type") val attentionType: Int,
)

data class Neighbor(
    val id: Int? = null,
    val name: String,
    val rut: String,
    val address: String = "",
    val phone: String = "",
    val territory: Int,
    @SerializedName("management_type") val managementType: String = "",
    val status: String = "Activo",
)

data class Attention(
    val id: Int? = null,
    val neighbor: Int,
    @SerializedName("attention_type") val attentionType: Int,
    @SerializedName("attention_subtype") val attentionSubtype: Int? = null,
    val delegation: Int,
    val staff: Int,
    val date: String? = null,
    val reason: String,
    val detail: String = "",
    val status: String = "ingresada",
    val channel: String = "",
)
