package com.example.proyectopenka.data

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Cliente único contra la API de abstergo. */
object Api {

    // Host de desarrollo con certificado autofirmado (ver network_security_config).
    const val BASE_URL = "https://18.209.251.129/"

    lateinit var sesion: Sesion
        private set

    fun init(context: Context) {
        if (!::sesion.isInitialized) {
            sesion = Sesion(context.applicationContext)
        }
    }

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
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
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
