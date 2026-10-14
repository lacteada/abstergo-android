package cl.lacteada.abstergo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Las lecturas de los sensores IoT, desde `www.pnk.cl`.
 *
 * Trae temperatura y humedad cada pocos segundos, y trae dos controles:
 * la linterna real del teléfono y una ampolleta que es solo visual.
 */
class Sensors : ActividadBase() {

    private lateinit var fecha: TextView
    private lateinit var txtTemp: TextView
    private lateinit var txtHumedad: TextView
    private lateinit var imagenTemp: ImageView
    private lateinit var imgLinterna: ImageView
    private lateinit var imgAmpolleta: ImageView
    private lateinit var txtAmpolleta: TextView
    private lateinit var btnBack: Button

    private val cliente = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private var camara: CameraManager? = null
    private var idFlash: String? = null
    private var linternaEncendida = false
    private var ampolletaEncendida = false

    private val pedirCamara = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) alternarLinterna()
        else aviso("Permiso denegado", "Sin cámara no se puede encender la linterna.")
    }

    private val reloj = object : Runnable {
        override fun run() {
            fecha.text = horaActual()
            handler.postDelayed(this, MILISEGUNDOS_RELOJ)
        }
    }

    private val lecturas = object : Runnable {
        override fun run() {
            consultarSensores()
            handler.postDelayed(this, MILISEGUNDOS_LECTURA)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensors)

        fecha = findViewById(R.id.txt_fecha)
        txtTemp = findViewById(R.id.txt_temp)
        txtHumedad = findViewById(R.id.txt_humedad)
        imagenTemp = findViewById(R.id.imagen_temp)
        imgLinterna = findViewById(R.id.imgNoti)
        imgAmpolleta = findViewById(R.id.imageView9)
        txtAmpolleta = findViewById(R.id.txtUVVal)
        btnBack = findViewById(R.id.btnbacksensors)

        imgLinterna.setImageResource(R.drawable.flashlightoff)
        imgAmpolleta.setImageResource(R.drawable.bulboff)

        camara = getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        idFlash = camara?.cameraIdList?.firstOrNull { tieneFlash(it) }

        btnBack.setOnClickListener { finish() }
        imgLinterna.setOnClickListener { pedirLinterna() }
        imgAmpolleta.setOnClickListener { alternarAmpolleta() }
    }

    override fun onResume() {
        super.onResume()
        reloj.run()
        lecturas.run()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(reloj)
        handler.removeCallbacks(lecturas)
    }

    private fun tieneFlash(id: String): Boolean = try {
        camara?.getCameraCharacteristics(id)?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    } catch (e: Exception) {
        false
    }

    private fun horaActual(): String =
        SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale("es", "CL")).format(Date())

    private fun consultarSensores() {
        cliente.newCall(Request.Builder().url(URL_SENSORES).build()).enqueue(
            object : Callback {
                override fun onFailure(llamada: Call, e: IOException) {
                    runOnUiThread { mostrarSinDatos() }
                }

                override fun onResponse(llamada: Call, respuesta: Response) {
                    val cuerpo = respuesta.body?.string().orEmpty()
                    runOnUiThread { pintarLectura(cuerpo) }
                }
            }
        )
    }

    private fun pintarLectura(cuerpo: String) {
        try {
            val json = JSONObject(cuerpo)
            val temperatura = json.getString("temperatura")
            txtTemp.text = "$temperatura °C"
            txtHumedad.text = "${json.getString("humedad")} %"
            val valor = temperatura.toDoubleOrNull()
            imagenTemp.setImageResource(
                if (valor != null && valor >= UMBRAL_TEMPERATURA) R.drawable.temperature_high
                else R.drawable.temperature_low
            )
        } catch (e: Exception) {
            // La API respondió algo que no entendemos: no se cae la pantalla.
            mostrarSinDatos()
        }
    }

    private fun mostrarSinDatos() {
        txtTemp.text = "—"
        txtHumedad.text = "—"
    }

    private fun pedirLinterna() {
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            alternarLinterna()
        } else {
            pedirCamara.launch(Manifest.permission.CAMERA)
        }
    }

    private fun alternarLinterna() {
        val id = idFlash
        if (id == null) {
            aviso("Sin linterna", "Este teléfono no tiene flash.")
            return
        }
        try {
            linternaEncendida = !linternaEncendida
            camara?.setTorchMode(id, linternaEncendida)
            imgLinterna.setImageResource(
                if (linternaEncendida) R.drawable.flashlighton else R.drawable.flashlightoff
            )
        } catch (e: Exception) {
            linternaEncendida = false
            imgLinterna.setImageResource(R.drawable.flashlightoff)
            aviso("No se pudo", "El teléfono no dejó encender la linterna.")
        }
    }

    private fun alternarAmpolleta() {
        // Es solo visual: no enciende la linterna ni controla nada físico.
        ampolletaEncendida = !ampolletaEncendida
        imgAmpolleta.setImageResource(
            if (ampolletaEncendida) R.drawable.bulbon else R.drawable.bulboff
        )
        txtAmpolleta.text = if (ampolletaEncendida) "Ampolleta encendida" else "Ampolleta apagada"
    }

    private companion object {
        const val URL_SENSORES = "https://www.pnk.cl/muestra_datos.php"
        const val UMBRAL_TEMPERATURA = 20.0
        const val MILISEGUNDOS_RELOJ = 1_000L
        const val MILISEGUNDOS_LECTURA = 5_000L
    }
}
