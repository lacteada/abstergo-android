package cl.lacteada.abstergo

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.lacteada.abstergo.data.BdLocal
import cl.lacteada.abstergo.data.Conectividad
import cl.lacteada.abstergo.data.Sincronizador
import cl.lacteada.abstergo.data.UsuarioLocal
import cl.lacteada.abstergo.dominio.sinAcentos
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch

/**
 * El listado de usuarios.
 *
 * Lee de la base local (funciona sin conexión) y, cuando hay Internet, sube lo
 * pendiente a MySQL y se trae la lista actualizada.
 */
class UsuarioCrud : ActividadBase() {

    private lateinit var btnBack: Button
    private lateinit var btnNuevo: Button
    private lateinit var searchInput: EditText
    private lateinit var txtEstado: TextView
    private lateinit var adapter: ListaAdapter<UsuarioLocal>
    private lateinit var bd: BdLocal

    private var usuarios: List<UsuarioLocal> = emptyList()
    private var observador: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)

        btnBack = findViewById(R.id.btnbackusuarios)
        btnNuevo = findViewById(R.id.btnnuevousuario)
        searchInput = findViewById(R.id.searchInputUsuarios)
        txtEstado = findViewById(R.id.txtEstadoSync)

        bd = BdLocal(this)

        adapter = ListaAdapter(
            describir = { usuario ->
                val marca = if (usuario.pendiente) " · pendiente" else ""
                "${usuario.nombre} ${usuario.apellido} · ${usuario.email}$marca"
            },
            alTocar = { mostrarOpciones(it) },
        )
        findViewById<RecyclerView>(R.id.listaUsuarios).apply {
            layoutManager = LinearLayoutManager(this@UsuarioCrud)
            adapter = this@UsuarioCrud.adapter
        }

        if (!Permisos.puedeEscribir("users")) btnNuevo.visibility = View.GONE

        btnBack.setOnClickListener { finish() }
        btnNuevo.setOnClickListener { startActivity(Intent(this, UsuarioForm::class.java)) }
        searchInput.addTextChangedListener(filtro { filtrar(it) })

        // Al volver Internet, se sincroniza solo.
        observador = Conectividad.observar(this) {
            pintarEstado()
            sincronizar()
        }
    }

    override fun onResume() {
        super.onResume()
        if (irALoginSiExpiro()) return
        cargar()
        sincronizar()
    }

    override fun onDestroy() {
        super.onDestroy()
        Conectividad.dejarDeObservar(this, observador)
        bd.close()
    }

    private fun cargar() {
        usuarios = bd.usuarios()
        filtrar(searchInput.text.toString())
        pintarEstado()
    }

    /** Le dice al usuario en qué estado está: sin conexión, pendiente o al día. */
    private fun pintarEstado() {
        val pendientes = bd.cantidadPendientes()
        txtEstado.text = when {
            !Conectividad.hayRed(this) -> "Sin conexión · $pendientes pendiente(s)"
            pendientes > 0 -> "$pendientes cambio(s) esperando al servidor"
            else -> "Todo sincronizado con MySQL"
        }
    }

    private fun sincronizar() {
        lifecycleScope.launch {
            val resultado = Sincronizador.sincronizar(this@UsuarioCrud, bd)
            cargar()
            if (resultado.sincronizados > 0 && resultado.error == null) {
                aviso(
                    "Sincronización completada",
                    "${resultado.sincronizados} cambio(s) subido(s) a MySQL.",
                    SweetAlertDialog.SUCCESS_TYPE,
                )
            }
        }
    }

    private fun filtrar(consulta: String) {
        // Sin distinguir mayúsculas ni acentos, como pide la pauta.
        val texto = sinAcentos(consulta.trim())
        val visibles = if (texto.isEmpty()) {
            usuarios
        } else {
            usuarios.filter {
                sinAcentos("${it.nombre} ${it.apellido}").contains(texto) ||
                    it.email.lowercase().contains(texto)
            }
        }
        adapter.setDatos(visibles)
    }

    private fun mostrarOpciones(usuario: UsuarioLocal) {
        val rotulo = "${usuario.nombre} ${usuario.apellido}"
        if (!Permisos.puedeEscribir("users")) {
            aviso(rotulo, "${usuario.email} · Tu rol solo permite ver.", SweetAlertDialog.WARNING_TYPE)
            return
        }
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(rotulo)
            .setContentText("${usuario.email} · ¿Qué desea hacer?")
            .setConfirmText("Editar")
            .setCancelText("Eliminar")
            .setConfirmClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                startActivity(
                    Intent(this, UsuarioForm::class.java).putExtra("idLocal", usuario.idLocal)
                )
            }
            .setCancelClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                confirmarEliminar(usuario)
            }
            .show()
    }

    private fun confirmarEliminar(usuario: UsuarioLocal) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Eliminar a ${usuario.nombre}?")
            .setContentText("Esta acción no se puede deshacer.")
            .setConfirmText("Eliminar")
            .setCancelText("Cancelar")
            .setConfirmClickListener { dialogo ->
                dialogo.dismissWithAnimation()
                eliminar(usuario)
            }
            .show()
    }

    private fun eliminar(usuario: UsuarioLocal) {
        if (usuario.idRemoto == null) {
            // Nunca llegó al servidor: alcanza con borrarlo local.
            bd.eliminar(usuario.idLocal)
        } else {
            bd.guardar(
                usuario.copy(
                    estado = UsuarioLocal.PENDIENTE_BORRAR,
                    actualizado = System.currentTimeMillis(),
                )
            )
        }
        cargar()
        sincronizar()
    }
}
