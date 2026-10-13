package cl.lacteada.abstergo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cl.lacteada.abstergo.data.Usuario

class UsuarioAdapter(private val alTocar: (Usuario) -> Unit) :
    RecyclerView.Adapter<UsuarioAdapter.Fila>() {

    private var datos: List<Usuario> = emptyList()

    fun setDatos(nuevos: List<Usuario>) {
        datos = nuevos
        notifyDataSetChanged()
    }

    class Fila(vista: View) : RecyclerView.ViewHolder(vista) {
        val texto: TextView = vista.findViewById(R.id.filaUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Fila {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return Fila(vista)
    }

    override fun getItemCount(): Int = datos.size

    override fun onBindViewHolder(holder: Fila, position: Int) {
        val usuario = datos[position]
        val estado = if (usuario.activo) "activo" else "inactivo"
        holder.texto.text = "${usuario.nombre} ${usuario.apellido} · ${usuario.email ?: "sin correo"} · $estado"
        holder.texto.setOnClickListener { alTocar(usuario) }
    }
}
