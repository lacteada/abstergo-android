package cl.lacteada.abstergo.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/v1/auth/login/")
    suspend fun login(@Body cuerpo: LoginRequest): TokenResponse

    @POST("api/v1/auth/refresh/")
    suspend fun refrescar(@Body cuerpo: RefreshRequest): TokenResponse

    /** Versión síncrona, para el Authenticator de OkHttp. */
    @POST("api/v1/auth/refresh/")
    fun refrescarSync(@Body cuerpo: RefreshRequest): Call<TokenResponse>

    @POST("api/v1/auth/logout/")
    suspend fun cerrarSesion(@Body cuerpo: RefreshRequest)

    @GET("api/v1/auth/me/")
    suspend fun yo(): Usuario

    @POST("api/v1/auth/recuperar/")
    suspend fun recuperar(@Body cuerpo: RecuperarRequest): Detalle

    @POST("api/v1/auth/validar/")
    suspend fun validar(@Body cuerpo: ValidarRequest): Detalle

    @POST("api/v1/auth/nueva-password/")
    suspend fun nuevaPassword(@Body cuerpo: NuevaPasswordRequest): Detalle

    @GET("api/v1/usuarios/")
    suspend fun usuarios(): Pagina<Usuario>

    @POST("api/v1/usuarios/")
    suspend fun crearUsuario(@Body cuerpo: UsuarioRequest): Usuario

    @PATCH("api/v1/usuarios/{id}/")
    suspend fun actualizarUsuario(@Path("id") id: Int, @Body cuerpo: UsuarioRequest): Usuario

    @DELETE("api/v1/usuarios/{id}/")
    suspend fun eliminarUsuario(@Path("id") id: Int)

    @GET("api/v1/roles/")
    suspend fun roles(): Pagina<Rol>

    @GET("api/v1/delegaciones/")
    suspend fun delegaciones(): Pagina<Delegacion>

    @GET("api/v1/vecinos/")
    suspend fun vecinos(): Pagina<Vecino>

    @POST("api/v1/vecinos/")
    suspend fun crearVecino(@Body cuerpo: VecinoRequest): Vecino

    @PATCH("api/v1/vecinos/{id}/")
    suspend fun actualizarVecino(@Path("id") id: Int, @Body cuerpo: VecinoRequest): Vecino

    @DELETE("api/v1/vecinos/{id}/")
    suspend fun eliminarVecino(@Path("id") id: Int)

    @GET("api/v1/tipos-atencion/")
    suspend fun tiposAtencion(): Pagina<TipoAtencion>

    @GET("api/v1/sub-atenciones/")
    suspend fun subAtenciones(): Pagina<SubAtencion>

    @GET("api/v1/atenciones/")
    suspend fun atenciones(): Pagina<Atencion>

    @POST("api/v1/atenciones/")
    suspend fun crearAtencion(@Body cuerpo: AtencionRequest): Atencion

    @PATCH("api/v1/atenciones/{id}/")
    suspend fun actualizarAtencion(@Path("id") id: Int, @Body cuerpo: AtencionRequest): Atencion

    @DELETE("api/v1/atenciones/{id}/")
    suspend fun eliminarAtencion(@Path("id") id: Int)
}
