package cl.lacteada.abstergo.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** El contrato de `/api/v1/`. Las rutas y los campos están en inglés. */
interface ApiService {

    // --------------------------------------------------------- Autenticación
    @POST("api/v1/auth/login/")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("api/v1/auth/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): TokenResponse

    /** Igual que `refresh`, pero sincrónica: la usa el `Authenticator` de OkHttp. */
    @POST("api/v1/auth/refresh/")
    fun refreshSync(@Body body: RefreshRequest): Call<TokenResponse>

    @POST("api/v1/auth/logout/")
    suspend fun logout(@Body body: RefreshRequest)

    @GET("api/v1/auth/me/")
    suspend fun me(): Profile

    @POST("api/v1/auth/recover/")
    suspend fun recover(@Body body: RecoverRequest): Message

    @POST("api/v1/auth/register/")
    suspend fun register(@Body body: RegisterRequest): User

    @POST("api/v1/auth/verify/")
    suspend fun verify(@Body body: VerifyRequest): Message

    @POST("api/v1/auth/new-password/")
    suspend fun newPassword(@Body body: NewPasswordRequest): Message

    // ------------------------------------------------------------ Catálogos
    @GET("api/v1/roles/")
    suspend fun roles(): Page<Role>

    @GET("api/v1/delegations/")
    suspend fun delegations(): Page<Delegation>

    @GET("api/v1/attention-types/")
    suspend fun attentionTypes(): Page<AttentionType>

    @GET("api/v1/attention-subtypes/")
    suspend fun attentionSubtypes(): Page<AttentionSubtype>

    // ------------------------------------------------------------- Usuarios
    @GET("api/v1/users/")
    suspend fun users(): Page<User>

    @POST("api/v1/users/")
    suspend fun createUser(@Body body: User): User

    @PATCH("api/v1/users/{id}/")
    suspend fun updateUser(@Path("id") id: Int, @Body body: User): User

    @DELETE("api/v1/users/{id}/")
    suspend fun deleteUser(@Path("id") id: Int)

    // -------------------------------------------------------------- Vecinos
    @GET("api/v1/neighbors/")
    suspend fun neighbors(): Page<Neighbor>

    @POST("api/v1/neighbors/")
    suspend fun createNeighbor(@Body body: Neighbor): Neighbor

    @PATCH("api/v1/neighbors/{id}/")
    suspend fun updateNeighbor(@Path("id") id: Int, @Body body: Neighbor): Neighbor

    @DELETE("api/v1/neighbors/{id}/")
    suspend fun deleteNeighbor(@Path("id") id: Int)

    // ------------------------------------------------------------ Atenciones
    @GET("api/v1/cases/")
    suspend fun cases(): Page<Attention>

    @POST("api/v1/cases/")
    suspend fun createCase(@Body body: Attention): Attention

    @PATCH("api/v1/cases/{id}/")
    suspend fun updateCase(@Path("id") id: Int, @Body body: Attention): Attention

    @DELETE("api/v1/cases/{id}/")
    suspend fun deleteCase(@Path("id") id: Int)
}
