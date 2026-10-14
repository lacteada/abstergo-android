package cl.lacteada.abstergo.data

import android.content.Context
import cl.lacteada.abstergo.BuildConfig
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** El cliente único contra la API de abstergo. */
object Api {

    /** Sale de `buildConfigField`: cambia según la variante. */
    val BASE_URL: String = BuildConfig.BASE_URL

    lateinit var sesion: Sesion
        private set

    /** El perfil del usuario con su rol y módulos. Lo carga el menú. */
    @Volatile
    var perfil: Profile? = null

    fun init(context: Context) {
        if (!::sesion.isInitialized) {
            sesion = Sesion(context.applicationContext)
        }
    }

    private val gson = GsonConverterFactory.create()

    // Cliente sin autenticador, para pedir el refresh sin recursión.
    private val clienteRefresh: OkHttpClient by lazy { OkHttpClient.Builder().build() }

    private val servicioRefresh: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clienteRefresh)
            .addConverterFactory(gson)
            .build()
            .create(ApiService::class.java)
    }

    /** Ante un 401, renueva el token y reintenta una vez. Si no puede, cierra la sesión. */
    private val autenticador = Authenticator { _, respuesta ->
        if (respuesta.priorResponse != null) return@Authenticator null

        val refresh = sesion.refresh ?: return@Authenticator null
        try {
            val llamada = servicioRefresh.refreshSync(RefreshRequest(refresh)).execute()
            val cuerpo = llamada.body()
            if (llamada.isSuccessful && cuerpo != null) {
                sesion.guardar(cuerpo.access, cuerpo.refresh ?: refresh)
                respuesta.request.newBuilder()
                    .header("Authorization", "Bearer ${cuerpo.access}")
                    .build()
            } else {
                sesion.limpiar()
                null
            }
        } catch (e: Exception) {
            sesion.limpiar()
            null
        }
    }

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .authenticator(autenticador)
            .addInterceptor { cadena ->
                val peticion = cadena.request().newBuilder().apply {
                    sesion.access?.let { header("Authorization", "Bearer $it") }
                }.build()
                cadena.proceed(peticion)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()
    }

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(gson)
            .build()
            .create(ApiService::class.java)
    }
}
