package com.example.proyectopenka

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import kotlin.random.Random

class Sensors : AppCompatActivity() {

    private lateinit var datos: RequestQueue
    private lateinit var fecha: TextView
    private lateinit var btnBack: Button
    private lateinit var txt_temp: TextView
    private lateinit var txt_humedad: TextView
    private lateinit var imagen_temp: ImageView
    private lateinit var txtLuzVal: TextView
    private lateinit var imgNoti: ImageView
    private var notiOn = false

    private val handler = Handler(Looper.getMainLooper())
    private val intervaloMs = 1_000L
    private val tareaReloj = object : Runnable {
        override fun run() {
            fecha.text = fechahora()
            handler.postDelayed(this, intervaloMs)
        }
    }
    private val tareaDatos = object : Runnable {
        override fun run() {
            obtenerDatos()
            handler.postDelayed(this, intervaloMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sensors)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fecha = findViewById(R.id.txt_fecha)
        txt_temp = findViewById(R.id.txt_temp)
        txt_humedad = findViewById(R.id.txt_humedad)
        imagen_temp = findViewById(R.id.imagen_temp)
        btnBack = findViewById(R.id.btnbacksensors)
        txtLuzVal = findViewById(R.id.txtUVVal)
        imgNoti = findViewById(R.id.imgNoti)

        datos = Volley.newRequestQueue(this)

        btnBack.setOnClickListener { finish() }

        imgNoti.setOnClickListener {
            notiOn = !notiOn
            imgNoti.setImageResource(if (notiOn) R.drawable.noti else R.drawable.notioff)
        }

        tareaReloj.run()
        tareaDatos.run()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tareaReloj)
        handler.removeCallbacks(tareaDatos)
    }

    private fun fechahora(): String {
        val c: Calendar = Calendar.getInstance()
        val sdf: SimpleDateFormat = SimpleDateFormat("dd MMMM YYYY, hh:mm:ss a ")
        val strDate: String = sdf.format(c.getTime())
        return strDate
    }

    private fun obtenerDatos() {
        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    val tempStr = response.getString("temperatura")
                    txt_temp.text = "$tempStr C"
                    txt_humedad.text = "${response.getString("humedad")} %"

                    val tempVal = tempStr.toDoubleOrNull()
                    imagen_temp.setImageResource(
                        if (tempVal != null && tempVal >= TEMP_UMBRAL) {
                            R.drawable.temperature_high
                        } else {
                            R.drawable.temperature_low
                        }
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                error.printStackTrace()
            }
        )
        datos.add(request)
    }

    companion object {
        private const val TEMP_UMBRAL = 20.0
    }
}
