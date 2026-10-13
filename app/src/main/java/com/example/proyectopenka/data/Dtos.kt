package com.example.proyectopenka.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(val email: String, val password: String)

data class RefreshRequest(val refresh: String)

data class TokenResponse(val access: String, val refresh: String?)

data class Detalle(val detail: String)

data class RecuperarRequest(val email: String)

data class ValidarRequest(val email: String, val codigo: String)

data class NuevaPasswordRequest(val email: String, val codigo: String, val password: String)

data class Pagina<T>(val count: Int, val results: List<T>)

data class Usuario(
    val id: Int,
    val email: String?,
    @SerializedName("first_name") val nombre: String,
    @SerializedName("last_name") val apellido: String,
    val rol: Int?,
    val delegacion: Int?,
    @SerializedName("is_active") val activo: Boolean,
)

data class UsuarioRequest(
    val email: String?,
    @SerializedName("first_name") val nombre: String,
    @SerializedName("last_name") val apellido: String,
    val rol: Int?,
    val delegacion: Int?,
    val password: String?,
)

data class Rol(val id: Int, val codigo: String?, val nombre: String, val descripcion: String?)

data class Delegacion(val id: Int, val codigo: String, val nombre: String, val comuna: String)

data class Vecino(
    val id: Int,
    val nombre: String,
    val rut: String,
    val direccion: String,
    val telefono: String,
    val territorio: Int,
    val tipo_gestion: String,
    val estado: String,
)

data class VecinoRequest(
    val nombre: String,
    val rut: String,
    val direccion: String,
    val telefono: String,
    val territorio: Int,
    val tipo_gestion: String,
    val estado: String,
)
